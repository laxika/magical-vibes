package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * "You may cast a [filter] spell from your hand without paying its mana cost."
 *
 * <p>Reuses the Counterlash may-cast-from-hand routing: one {@link PendingMayAbility} is queued per
 * eligible nonland hand card, and accepting one casts it for free while clearing the remaining
 * offers. A null filter (Maelstrom Archangel) matches every nonland; Wildfire Eternal passes an
 * instant-or-sorcery filter.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayCastAnySpellFromHandWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;
    private final com.github.laxika.magicalvibes.service.CardRevealService cardRevealService;
    private final com.github.laxika.magicalvibes.service.cast.CastingCostService castingCostService;
    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private SplitCardCastSupport splitCardCastSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayCastAnySpellFromHandWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MayCastAnySpellFromHandWithoutPayingManaCostEffect e =
                (MayCastAnySpellFromHandWithoutPayingManaCostEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand == null || hand.isEmpty()) return;
        if (e.revealDrawnCards()) {
            cardRevealService.revealToAllPlayers(gameData, controllerId,
                    com.github.laxika.magicalvibes.model.event.GameEventFact.RevealZone.HAND,
                    hand.stream().filter(card -> entry.getDrawnCardIdsThisResolution().contains(card.getId())).toList());
        }
        int maxManaValue = e.maxManaValue() == null
                ? Integer.MAX_VALUE
                : amountEvaluationService.evaluate(gameData, e.maxManaValue(),
                        AmountContext.forStackEntry(entry, entry.getSourcePermanentId() == null
                                ? null
                                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())));

        // "A spell with [characteristics]" considers only the half of a split card being cast
        // (CR 709.3a), so a split card is offered as the halves that qualify.
        java.util.function.Predicate<Card> qualifies = spell -> spell.getManaValue() <= maxManaValue
                && predicateEvaluationService.matchesCardPredicate(spell, e.spellFilter(), null, gameData, controllerId);
        Map<Card, List<Integer>> eligible = new LinkedHashMap<>();
        List<Card> eligibleAdventures = new ArrayList<>();
        hand.stream()
                .filter(c -> !e.drawnCardsOnly() || entry.getDrawnCardIdsThisResolution().contains(c.getId()))
                .filter(c -> !c.hasType(CardType.LAND))
                .filter(c -> castingCostService.canPayAdditionalSpellCosts(gameData, controllerId, c))
                .forEach(c -> {
                    if (SplitCardCastSupport.isSplitCard(c)) {
                        List<Integer> modes = splitCardCastSupport.modesMatching(gameData, c, controllerId, qualifies);
                        if (!modes.isEmpty()) {
                            eligible.put(c, modes);
                        }
                    } else if (qualifies.test(c)) {
                        eligible.put(c, null);
                    }
                    // A modal double-faced card may be cast as either face, and the face being cast is
                    // the one judged against the limit.
                    Card backFace = c.getBackFaceCard();
                    if (c.isModalDoubleFaced() && backFace != null && !backFace.hasType(CardType.LAND)
                            && qualifies.test(backFace)) {
                        Card backFaceCopy = c.createRuntimeCopyWithFace(backFace);
                        if (castingCostService.canPayAdditionalSpellCosts(gameData, controllerId, backFaceCopy)) {
                            eligible.put(backFaceCopy, null);
                        }
                    }
                    // CR 715.3a: an adventurer card cast as an Adventure is judged by its Adventure's
                    // characteristics alone.
                    if (c.getCastingOption(AdventureCast.class).isPresent() && c.getBackFaceCard() != null
                            && qualifies.test(c.getBackFaceCard())) {
                        eligibleAdventures.add(c);
                    }
                });

        UUID choiceGroupId = UUID.randomUUID();
        for (int i = eligibleAdventures.size() - 1; i >= 0; i--) {
            Card c = eligibleAdventures.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(
                            e.revealDrawnCards(), choiceGroupId, null, false, e.afterSuccessfulCastEffect())
                            .asAdventure()),
                    "Cast " + c.getBackFaceCard().getName() + " without paying its mana cost?",
                    entry.getSourcePermanentId(), (Integer) null
            ));
        }
        List<Card> cards = new ArrayList<>(eligible.keySet());
        for (int i = cards.size() - 1; i >= 0; i--) {
            Card c = cards.get(i);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    c, controllerId,
                    List.of(new MayCastFromHandWithoutPayingManaCostEffect(
                            e.revealDrawnCards(), choiceGroupId, null, false, e.afterSuccessfulCastEffect())
                            .withCastableSplitModes(eligible.get(c))),
                    "Cast " + c.getName() + " without paying its mana cost?",
                    entry.getSourcePermanentId(), (Integer) null
            ));
        }
    }
}
