package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromChosenSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link PreventDamageFromChosenSourceEffect}: collects the legal source choices
 * (optionally restricted by the effect's source filter), then starts a permanent choice whose
 * context installs the scope-appropriate prevention shield once the source is picked (see
 * {@code PermanentChoiceBattlefieldHandlerService}).
 */
@Component
@RequiredArgsConstructor
public class PreventDamageFromChosenSourceEffectHandler implements NormalEffectHandlerBean {

    private final PreventionSupport preventionSupport;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PreventDamageFromChosenSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PreventDamageFromChosenSourceEffect) effect;
        UUID controllerId = entry.getControllerId();

        PermanentPredicate sourceFilter = e.sourceFilter();
        String sourceLabel = e.sourceLabel();
        if (e.sourceSharesColorWithImprintedCard()) {
            Card imprintedCard = gameData.getImprintedCard(entry.getCard());
            if (imprintedCard == null || gameData.findExiledCard(imprintedCard.getId()) == null
                    || imprintedCard.getColors() == null || imprintedCard.getColors().isEmpty()) {
                preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
                return;
            }
            sourceFilter = new PermanentColorInPredicate(Set.copyOf(imprintedCard.getColors()));
        } else if (e.sourceChosenColor()) {
            Permanent source = entry.getSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            CardColor chosenColor = source == null ? null : source.getChosenColor();
            if (chosenColor == null) {
                preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
                return;
            }
            sourceFilter = new PermanentColorInPredicate(Set.of(chosenColor));
            sourceLabel = chosenColor.name().toLowerCase(Locale.ROOT);
        } else if (e.sourceActivationManaColor()) {
            Set<CardColor> activationColors = EnumSet.noneOf(CardColor.class);
            for (ManaColor manaColor : entry.getActivationManaSpent().keySet()) {
                if (manaColor != ManaColor.COLORLESS) {
                    activationColors.add(CardColor.valueOf(manaColor.name()));
                }
            }
            sourceFilter = new PermanentColorInPredicate(activationColors);
        }

        List<UUID> validIds = collectValidSourceIds(gameData, sourceFilter);
        if (validIds.isEmpty()) {
            preventionSupport.broadcastNoPermanentsForDamageSourceChoice(gameData);
            return;
        }

        String label = sourceLabel == null ? "" : sourceLabel + " ";
        PermanentChoiceContext context;
        String prompt;
        switch (e.scope()) {
            case NEXT_DAMAGE_TO_CONTROLLER -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceChoice(
                        controllerId, e.gainLife(), e.exileFromLibrary(),
                        e.damageSourceController() ? entry.getCard() : null);
                String rider = e.damageSourceController()
                        ? " If damage is prevented this way, this spell deals that much damage to that source's controller."
                        : e.gainLife()
                        ? " and gain that much life."
                        : e.exileFromLibrary()
                                ? " and exile that many cards from the top of your library."
                                : ".";
                prompt = "Choose a " + label
                        + "source. The next time it would deal damage to you this turn, prevent that damage"
                        + rider;
            }
            case NEXT_DAMAGE_TO_ANY_TARGET -> {
                context = e.damageRedSourceController()
                        ? new PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice(
                                controllerId, true, entry.getCard())
                        : new PermanentChoiceContext.PreventNextDamageFromSourceToAnyTargetChoice(controllerId);
                prompt = e.damageRedSourceController()
                        ? "Choose a source. The next time it would deal damage to any target this turn, prevent that damage."
                                + " If it is red, Honorable Passage deals that much damage to its controller."
                        : "Choose a source. The next time it would deal damage to any target this turn, prevent that damage.";
            }
            case NEXT_DAMAGE_TO_CONTROLLER_AND_CREATURES -> {
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToYouAndYourCreaturesChoice(controllerId);
                prompt = "Choose a source. The next time it would deal damage to you and/or creatures you control"
                        + " this turn, prevent that damage. If that source is black, you gain that much life.";
            }
            case NEXT_DAMAGE_TO_ENCHANTED -> {
                UUID enchantedId = findEnchantedCreatureId(gameData, entry);
                if (enchantedId == null) {
                    return;
                }
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice(
                        controllerId, enchantedId);
                prompt = "Choose a source. The next time it would deal damage to enchanted creature this turn,"
                        + " prevent that damage.";
            }
            case NEXT_DAMAGE_TO_TARGET_CREATURE -> {
                UUID targetId = entry.getTargetId();
                if (targetId == null) {
                    return;
                }
                context = new PermanentChoiceContext.PreventNextDamageFromSourceToPermanentChoice(
                        controllerId, targetId);
                prompt = "Choose a source. The next time it would deal damage to target creature this turn,"
                        + " prevent that damage.";
            }
            case ALL_DAMAGE_THIS_TURN -> {
                context = new PermanentChoiceContext.PreventDamageSourceChoice(
                        controllerId, e.controllerOnly(), e.gainLifeForBlackOrRedSource());
                prompt = e.sourceActivationManaColor()
                        ? "Choose a source that shares a color with the mana spent on this activation. Prevent all damage it would deal "
                                + (e.controllerOnly() ? "to you" : "") + " this turn."
                        : e.controllerOnly()
                        ? "Choose a source. Prevent all damage it would deal to you this turn."
                        : e.sourceSharesColorWithImprintedCard()
                                ? "Choose a source that shares a color with the exiled card. Prevent all damage it would deal this turn."
                                : "Choose a " + label + "source. Prevent all damage it would deal this turn.";
            }
            default -> throw new IllegalStateException("Unknown chosen-source prevention scope: " + e.scope());
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, controllerId, validIds, prompt);
    }

    /**
     * Resolves "enchanted creature" for the ability's source Aura. The Aura is usually sacrificed to
     * pay the activation cost, so the live permanent is gone by resolution; the attachment is read
     * from the last-known snapshot (CR 608.2h) with a live lookup as fallback.
     */
    private UUID findEnchantedCreatureId(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        Permanent aura = sourceId == null ? null : gameQueryService.findPermanentById(gameData, sourceId);
        UUID attachedTo = aura != null ? aura.getAttachedTo() : null;
        if (attachedTo == null && entry.getSourcePermanentSnapshot() != null) {
            attachedTo = entry.getSourcePermanentSnapshot().getAttachedTo();
        }
        if (attachedTo == null) {
            return null;
        }
        return gameQueryService.findPermanentById(gameData, attachedTo) == null ? null : attachedTo;
    }

    private List<UUID> collectValidSourceIds(GameData gameData, PermanentPredicate sourceFilter) {
        if (sourceFilter == null) {
            return preventionSupport.collectAllBattlefieldPermanentIds(gameData);
        }
        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, perm) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, perm, sourceFilter)) {
                validIds.add(perm.getId());
            }
        });
        return validIds;
    }
}
