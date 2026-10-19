package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileCastTargetSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileFreeCastQueueSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileNormalCostCopySupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SplitCardCastSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ExileTargetCardFromGraveyardAndMayCastCopyHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;
    private final SpellCastingService spellCastingService;
    private final ExileNormalCostCopySupport exileNormalCostCopySupport;
    @Autowired
    @Lazy
    private SplitCardCastSupport splitCardCastSupport;
    @Autowired
    @Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    @Lazy
    private ExileFreeCastQueueSupport exileFreeCastQueueSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCardFromGraveyardAndMayCastCopyEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Card copy = ability.sourceCard();
        ExileTargetCardFromGraveyardAndMayCastCopyEffect effect = ability.effects().stream()
                .filter(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::isInstance)
                .map(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();

        if (!accepted) {
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData,
                    GameLog.textCardText(player.getUsername() + " declines to cast the copy of ", copy, "."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ExiledCardEntry exiledEntry = gameData.findExiledCard(copy.getId());
        if (exiledEntry == null || copy.hasType(CardType.LAND)) {
            gameData.removeFromExile(copy.getId());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (SplitCardCastSupport.isSplitCard(copy)) {
            if (!splitCardCastSupport.beginFromExile(gameData, player.getId(), copy,
                    !effect.withoutPayingManaCost(),
                    SplitCardCast.ExileCast.ofCopy().losingLifeOnCast(effect.lifeLossOnCast()), Integer.MAX_VALUE,
                    effect.afterSuccessfulCastEffect(), ability.sourcePermanentId())) {
                gameLogService.append(gameData, GameLog.cardThen(copy,
                        " has no half that can be cast and the copy ceases to exist."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }
        if (effect.withoutPayingManaCost()) {
            gameData.exilePlayWithoutPayingManaCost.add(copy.getId());
            ChooseOneEffect modal = singleChoiceModal(copy);
            if (modal != null) {
                Map<String, Integer> modes = new LinkedHashMap<>();
                for (int mode = 0; mode < modal.options().size(); mode++) {
                    if (modal.options().get(mode).handOnly()) continue;
                    modes.put(modal.options().get(mode).label(), mode);
                }
                // CR 601.2b: a modal spell's mode is chosen as it is cast, before it can be responded to.
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        player.getId(), null, null,
                        new ChoiceContext.ExileCopyCastModeChoice(ability, modes),
                        new ArrayList<>(modes.keySet()), "Choose a mode for " + copy.getName() + "."));
                return;
            }
        } else if (exileNormalCostCopySupport.beginXValueChoice(gameData, player, copy, null, ability)) {
            return;
        }

        castCopy(gameData, player, ability, effect, 0);
    }

    /** Casts the copy with the mode the caster chose for it. */
    public void completeModeChoice(GameData gameData, Player player, String choice,
                                   ChoiceContext.ExileCopyCastModeChoice context) {
        Integer mode = context.modes().get(choice);
        if (mode == null) throw new IllegalStateException("Invalid mode choice");
        gameData.interaction.clearAwaitingInput();
        PendingMayAbility ability = context.ability();
        ExileTargetCardFromGraveyardAndMayCastCopyEffect effect = ability.effects().stream()
                .filter(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::isInstance)
                .map(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (gameData.findExiledCard(ability.sourceCard().getId()) == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        castCopy(gameData, player, ability, effect, mode);
    }

    private static ChooseOneEffect singleChoiceModal(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance).map(ChooseOneEffect.class::cast)
                .filter(modal -> modal.choicesRequired() == 1 && !modal.modesMayRepeat())
                .findFirst().orElse(null);
    }

    /** Resumes the copy cast once the caster has announced X for the cost being paid. */
    public void resumeCastWithX(GameData gameData, Player player, PendingMayAbility ability, int xValue) {
        Card copy = ability.sourceCard();
        ExileTargetCardFromGraveyardAndMayCastCopyEffect effect = ability.effects().stream()
                .filter(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::isInstance)
                .map(ExileTargetCardFromGraveyardAndMayCastCopyEffect.class::cast)
                .findFirst()
                .orElseThrow();
        if (gameData.findExiledCard(copy.getId()) == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        castCopy(gameData, player, ability, effect, xValue);
    }

    private void castCopy(GameData gameData, Player player, PendingMayAbility ability,
                          ExileTargetCardFromGraveyardAndMayCastCopyEffect effect, int xValue) {
        Card copy = ability.sourceCard();
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(copy);
        List<CardEffect> spellEffects = new ArrayList<>(copy.getEffects(EffectSlot.SPELL));
        if (EffectResolution.needsTarget(copy)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(
                    gameData, copy, player.getId());
            boolean hasLegalTargets = copy.getMaxTargets() > 1
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, copy, player.getId())
                    : !firstCandidates.isEmpty();
            if (!hasLegalTargets) {
                gameData.removeFromExile(copy.getId());
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    copy, player.getId(), spellEffects, spellType, true, List.of(), 0, true,
                    effect.lifeLossOnCast(), false, false, effect.afterSuccessfulCastEffect(),
                    ability.sourcePermanentId(), xValue));
            playerInputService.beginPermanentChoice(gameData, player.getId(), firstCandidates,
                    "Choose a target for " + copy.getName() + ".");
            return;
        }

        try {
            spellCastingService.playCardFromExileAsResolutionCast(
                    gameData, player, copy.getId(), xValue, (UUID) null, true);
            exileCastTargetSupport.queueAfterSuccessfulCast(gameData, copy, player.getId(),
                    ability.sourcePermanentId(), effect.afterSuccessfulCastEffect());
            applyLifeLoss(gameData, player, copy, effect.lifeLossOnCast());
        } catch (IllegalStateException ex) {
            if (effect.withoutPayingManaCost() && effect.lifeLossOnCast() == 0
                    && effect.afterSuccessfulCastEffect() == null
                    && ex.getMessage() != null && ex.getMessage().contains("additional cast cost is not supported")) {
                // The exile cast cannot pay this additional cost; the free-cast queue asks the caster to
                // choose how to pay it (Demand Answers' discard or sacrifice).
                exileFreeCastQueueSupport.queueCopiesForFreeCast(gameData, player.getId(), List.of(copy.getId()));
                return;
            }
            gameData.removeFromExile(copy.getId());
            gameLogService.append(gameData, GameLog.cardThen(copy,
                    " can't be cast and ceases to exist."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    public void applyLifeLoss(GameData gameData, Player player, Card copy, int amount) {
        if (amount > 0) {
            lifeSupport.applyLifeLoss(gameData, player.getId(), amount, copy.getName());
        }
    }
}
