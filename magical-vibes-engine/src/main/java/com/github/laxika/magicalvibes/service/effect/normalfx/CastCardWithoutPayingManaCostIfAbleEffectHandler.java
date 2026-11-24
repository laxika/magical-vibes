package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardWithoutPayingManaCostIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.PayFreeCastCostIncreaseEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.LibraryChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.SpellEvents;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CastCardWithoutPayingManaCostIfAbleEffect}: the card its subject names is cast by the player whose
 * zone it is in, during this resolution and without a "may" (CR 608.2g). An exiled card goes through the shared
 * exile free cast ({@link ExileFreeCastSupport}: Adventure and modal faces, split halves, targets, additional costs);
 * a card in a hand is judged by {@code LibraryChoiceHandlerService.canCastRevealedFromHandWithoutPaying} (permission,
 * its own timing restriction, additional costs, targets, CR 601.3), its cost increases are paid through
 * {@link FreeCastCostIncreaseSupport} (CR 601.2f, with mana abilities, CR 601.2g), a targeted spell is completed by the hand-cast
 * target choice and a required sacrifice by {@link ExileFreeCastQueueSupport#beginSacrificeCostIfNeeded}. A card that
 * can't be cast stays where it is. The step ends with any prompt it began, so it is a resolution's last step.
 */
@Component
@RequiredArgsConstructor
public class CastCardWithoutPayingManaCostIfAbleEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final ExileFreeCastSupport exileFreeCastSupport;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final SpellEvents spellEvents;
    @Autowired
    @Lazy
    private FreeCastCostIncreaseSupport freeCastCostIncreaseSupport;
    @Autowired
    @Lazy
    private InputCompletionService inputCompletionService;
    @Autowired
    @Lazy
    private SplitCardCastSupport splitCardCastSupport;
    @Autowired
    @Lazy
    private LibraryChoiceHandlerService libraryChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCardWithoutPayingManaCostIfAbleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var cast = (CastCardWithoutPayingManaCostIfAbleEffect) effect;
        List<SubjectResolutionService.LocatedCard> named =
                subjectResolutionService.locatedCards(gameData, entry, cast, cast.card());
        if (named.isEmpty()) {
            return;
        }
        SubjectResolutionService.LocatedCard located = named.getFirst();
        Card card = located.card();
        if (card.hasType(CardType.LAND)) {
            // A land is never a spell (CR 305.1), and one with another card type is played only as a land (CR 305.9).
            return;
        }
        Player caster = new Player(located.ownerId(), gameData.playerIdToName.get(located.ownerId()));
        if (located.zone() == Zone.EXILE) {
            ExiledCardEntry exiled = gameData.findExiledCard(card.getId());
            if (exiled == null || exiled.faceDown()) {
                return;
            }
            exileFreeCastSupport.castFromExileWithoutPaying(gameData, caster, card.getId(), false, false, false,
                    false);
        } else if (located.zone() == Zone.HAND) {
            castFromHand(gameData, caster, card);
        }
    }

    /**
     * Answers the cost-increase payment question of a paused hand cast: paid, the cast is made again with the mana now
     * in the pool; otherwise the card stays in the hand. Either way it ends through the input epilogue.
     */
    public void resumeAfterCostIncreaseDecision(GameData gameData, Player caster, Card card, boolean paid) {
        if (paid) {
            castFromHand(gameData, caster, card);
        } else {
            logUnpaidCostIncrease(gameData, card, caster.getUsername());
        }
        if (!gameData.interaction.isAwaitingInput()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    private void logUnpaidCostIncrease(GameData gameData, Card card, String playerName) {
        gameLogService.append(gameData, GameLog.builder().card(card)
                .text(" can't be cast: its cost increase isn't paid. It stays in " + playerName + "'s hand.")
                .build());
    }

    private void castFromHand(GameData gameData, Player caster, Card card) {
        UUID casterId = caster.getId();
        List<Card> hand = gameData.playerHands.get(casterId);
        int cardIndex = hand == null ? -1 : hand.indexOf(card);
        if (cardIndex < 0) {
            return;
        }
        String playerName = caster.getUsername();
        if (SplitCardCastSupport.isSplitCard(card)) {
            hand.remove(cardIndex);
            if (!splitCardCastSupport.begin(gameData, new SplitCardCast(casterId, card, Zone.HAND, cardIndex,
                    casterId, false, Integer.MAX_VALUE, null, null))) {
                gameLogService.append(gameData, GameLog.builder().card(card)
                        .text(" has no half that can be cast and stays in " + playerName + "'s hand.").build());
            }
            return;
        }
        if (!libraryChoiceHandlerService.canCastRevealedFromHandWithoutPaying(gameData, casterId, card)) {
            gameLogService.append(gameData, GameLog.builder().card(card)
                    .text(" can't be cast and stays in " + playerName + "'s hand.").build());
            return;
        }
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getCastingCostsAndSpellEffects());
        boolean targeted = EffectResolution.needsTarget(card) || EffectResolution.needsSpellTarget(card);
        List<UUID> validTargets = targeted
                ? libraryChoiceHandlerService.legalTargetsForCastWithoutPaying(gameData, casterId, card)
                : List.of();
        if (targeted && validTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.builder().card(card)
                    .text(" has no valid targets and stays in " + playerName + "'s hand.").build());
            return;
        }
        FreeCastCostIncreaseSupport.Payment payment = freeCastCostIncreaseSupport.payOrAsk(gameData, casterId, card,
                card.getId(), Zone.HAND, PayFreeCastCostIncreaseEffect.Resume.HAND_CAST, null);
        if (payment == FreeCastCostIncreaseSupport.Payment.ASKED) {
            return;
        }
        if (payment == FreeCastCostIncreaseSupport.Payment.UNPAYABLE) {
            logUnpaidCostIncrease(gameData, card, playerName);
            return;
        }
        hand.remove(cardIndex);
        if (targeted) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.HandCastSpellTarget(card, casterId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, casterId, validTargets,
                    "Choose a target for " + card.getName() + ".");
            gameLogService.append(gameData, GameLog.builder().text(playerName + " casts ").card(card)
                    .text(" without paying its mana cost — choosing target.").build());
            return;
        }
        StackEntry spell = new StackEntry(spellType, card, casterId, card.getName(), spellEffects, 0,
                (UUID) null, null);
        spell.setSourceZone(Zone.HAND);
        if (exileFreeCastQueueSupport.beginSacrificeCostIfNeeded(gameData, spell)) {
            return;
        }
        gameData.stack.add(spell);
        gameData.recordSpellCast(casterId, card);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.builder().text(playerName + " casts ").card(card)
                .text(" without paying its mana cost.").build());
        spellEvents.cast(gameData, card, casterId, Zone.HAND);
    }
}
