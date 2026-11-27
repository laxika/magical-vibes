package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Copies the spell a spell trigger is about ({@link CopyTriggeringSpellEffect}): for its controller, for each copier
 * the effect names in APNAP order (each asked first when the copy is optional), onto the trigger's source, or once for
 * each permanent the effect names that the spell could target.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTriggeringSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final CopyControllerCastSpellEffectHandler copyControllerCastSpellEffectHandler;
    private final TargetRedirectionSupport targetRedirectionSupport;
    private final SubjectResolutionService subjectResolutionService;
    private final ValidTargetService validTargetService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTriggeringSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "If you don't copy a spell this way" reads this step's result; it is false until a copy is made.
        entry.recordStep(StepKind.COPY_SPELL, StepOutcome.NONE);
        CopyTriggeringSpellEffect triggeringSpellEffect = (CopyTriggeringSpellEffect) effect;
        // The spell while it is still on the stack, else as it last existed there (CR 608.2h). A copy is copied too
        // when it is the triggering spell: a countered copy of Trial and Error triggers its own "copy it", and
        // "whenever you copy a spell" is about the copy.
        StackEntry spell = CopySupport.triggeringSpell(gameData, entry);
        if (spell == null || spell.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || spell.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
            return;
        }

        Card spellCard = spell.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        if (triggeringSpellEffect.retargetToSource()) {
            copyOntoSource(gameData, entry, triggeringSpellEffect, spell);
            return;
        }
        if (triggeringSpellEffect.copyTargets() != null) {
            copyOntoEachPermanent(gameData, entry, triggeringSpellEffect, spell);
            return;
        }

        List<UUID> copierIds = triggeringSpellEffect.copiers() == null
                ? List.of(spell.getControllerId())
                : subjectResolutionService.players(gameData, entry, effect, triggeringSpellEffect.copiers());
        if (copierIds.isEmpty()) {
            return;
        }
        if (triggeringSpellEffect.eachCopierMay()) {
            // Each copier decides in APNAP order and, saying yes, makes and retargets their copy before the next is
            // asked (CR 101.4); the copy is of the spell as it is when that copy is made.
            StackEntry spellSnapshot = new StackEntry(spell);
            List<CardEffect> offers = new ArrayList<>();
            for (UUID copierId : copierIds) {
                offers.add(MayEffect.forPlayer(controllerCopy(triggeringSpellEffect, spellSnapshot, copierId),
                        "Copy " + FaceDownVisibility.nameShownTo(gameData, spellSnapshot, copierId) + "?",
                        copierId));
            }
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, offers);
            return;
        }

        entry.recordStep(StepKind.COPY_SPELL, StepOutcome.ofObjects(List.of(spell.getTargetableId()), List.of()));
        // The shared copy machinery makes each copier's copies, with the effect's modifications, and offers the
        // new-target choice; copies made in APNAP order are decided and put on the stack in that order.
        for (UUID copierId : copierIds) {
            copyControllerCastSpellEffectHandler.resolve(gameData, entry,
                    controllerCopy(triggeringSpellEffect, spell, copierId));
        }
    }

    private static CopyControllerCastSpellEffect controllerCopy(CopyTriggeringSpellEffect e, StackEntry spell,
                                                                UUID copierId) {
        return new CopyControllerCastSpellEffect(spell, copierId, e.grantedKeywords(), e.additionalTypes(),
                e.removedSupertypes(), e.tokenCopy(), e.mayChooseNewTargets(), e.grantHasteToPermanentSpell(),
                e.startingLoyaltyFromX(), e.permanentSpellToken(), e.sacrificeAtEndStep(), e.additionalSubtypes(),
                e.powerOverride(), e.toughnessOverride(), e.copies(), e.dynamicCopies());
    }

    /** "The copy targets [this]": the ability's controller controls the copy, which targets the ability's source. */
    private void copyOntoSource(GameData gameData, StackEntry entry, CopyTriggeringSpellEffect effect,
                                StackEntry spell) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null || !targetRedirectionSupport.isValidNewTargetForSpell(
                gameData, spell, sourcePermanentId, entry.getControllerId())) {
            return;
        }
        entry.recordStep(StepKind.COPY_SPELL, StepOutcome.ofObjects(List.of(spell.getTargetableId()), List.of()));
        Card spellCard = spell.getCard();
        var copyCard = effect.tokenCopy()
                ? copySupport.createTokenCopyCard(spell)
                : copySupport.createCopyCard(spell);
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                spell, copyCard, entry.getControllerId(), spell.getTargetId());
        if (copyEntry.getTargetId() != null) {
            copyEntry.setTargetId(sourcePermanentId);
        }
        for (int i = 0; i < copyEntry.getDeclaredTargetIds().size(); i++) {
            copyEntry.replaceTargetIdAt(i, sourcePermanentId);
        }
        copySupport.addCopyToStack(gameData, copyEntry, (PendingMayAbility) null);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        log.info("Game {} - copy of {} created", gameData.id, spellCard.getName());
    }

    /**
     * "Copy the spell for each [permanent] that spell could target. Each copy targets a different one": one copy for
     * each permanent the effect names that the spell, as its controller would target it, could target; each copy has
     * that permanent as every one of its targets, with the whole divided damage, and is put on the stack at once (the
     * copier chooses no new targets). The copies are controlled by the copier the effect names, else the spell's
     * controller, who chooses the order they are put on the stack in (Precursor Golem and Ink-Treader Nephilim rulings),
     * through the trigger-order prompt, unless that player lets the game order their simultaneous stack objects.
     */
    private void copyOntoEachPermanent(GameData gameData, StackEntry entry, CopyTriggeringSpellEffect effect,
                                       StackEntry spell) {
        UUID copierId = effect.copiers() == null
                ? spell.getControllerId()
                : subjectResolutionService.player(gameData, entry, effect, effect.copiers());
        if (copierId == null) {
            return;
        }
        Set<UUID> couldTarget = validTargetService.permanentIdsSpellCouldTarget(gameData, spell);
        List<Permanent> targets = subjectResolutionService.permanents(gameData, entry, effect, effect.copyTargets())
                .stream().filter(permanent -> couldTarget.contains(permanent.getId())).toList();
        if (targets.isEmpty()) {
            return;
        }
        entry.recordStep(StepKind.COPY_SPELL, StepOutcome.ofObjects(List.of(spell.getTargetableId()), List.of()));
        Card spellCard = spell.getCard();
        boolean token = effect.tokenCopy() || effect.permanentSpellToken() && isPermanentSpell(spell.getEntryType());
        int dividedDamage = spell.getDamageAssignments().values().stream().mapToInt(Integer::intValue).sum();
        List<StackEntry> copies = new ArrayList<>();
        for (Permanent target : targets) {
            Card copyCard = token ? copySupport.createTokenCopyCard(spellCard) : copySupport.createCopyCard(spellCard);
            UUID targetId = target.getId();
            StackEntry copyEntry = copySupport.createCopyStackEntry(spell, copyCard, copierId, targetId,
                    spell.getDamageAssignments().isEmpty() ? Map.of() : Map.of(targetId, dividedDamage),
                    new ArrayList<>(Collections.nCopies(spell.getDeclaredTargetIds().size(), targetId)));
            copySupport.addCopyToStack(gameData, copyEntry);
            copies.add(copyEntry);
            gameLogService.append(gameData, GameLog.builder()
                    .text("A copy of ").card(spellCard)
                    .text(" is created targeting ").card(target.getCard()).text(".").build());
        }
        log.info("Game {} - {} copies of {} created, one targeting each of {} permanents",
                gameData.id, targets.size(), spellCard.getName(), targets.size());
        if (copies.size() > 1 && !gameData.playersAutoOrderingTriggers.contains(copierId)) {
            List<UUID> ids = copies.stream().map(StackEntry::getTargetableId).toList();
            String spellName = FaceDownVisibility.nameShownTo(gameData, spell, copierId);
            List<String> labels = IntStream.range(0, copies.size()).mapToObj(index -> (index + 1) + ": " + spellName
                    + " targeting " + FaceDownVisibility.nameShownTo(gameData, targets.get(index), copierId)).toList();
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(copierId, null, null,
                    new ChoiceContext.TriggerOrder(ids, List.of()), labels,
                    "Choose the copy to put on the stack first (it will resolve last)."));
        }
    }

    private static boolean isPermanentSpell(StackEntryType entryType) {
        return switch (entryType) {
            case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
            case TRIGGERED_ABILITY, ACTIVATED_ABILITY, INSTANT_SPELL, SORCERY_SPELL -> false;
        };
    }
}
