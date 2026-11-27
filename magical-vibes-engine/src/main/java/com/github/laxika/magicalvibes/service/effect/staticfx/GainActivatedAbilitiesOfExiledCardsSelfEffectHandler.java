package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintedCardXCostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService.LocatedCard;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gives the source the activated abilities of the cards a {@link GainActivatedAbilitiesOfExiledCardsEffect} names,
 * through the resolver's static card form ({@link SubjectResolutionService#namedCards}).
 */
@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfExiledCardsSelfEffectHandler implements StaticEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainActivatedAbilitiesOfExiledCardsEffect.class;
    }

    @Override
    public boolean selfOnly() {
        return true;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        GainActivatedAbilitiesOfExiledCardsEffect gainEffect = (GainActivatedAbilitiesOfExiledCardsEffect) effect;
        GameData gameData = context.gameData();
        for (LocatedCard located : lendingCards(gameData, gameQueryService, predicateEvaluationService,
                context.source(), context.sourceControllerId(), gainEffect.cards())) {
            Card card = located.card();
            if (gainEffect.filter() != null && !predicateEvaluationService.matchesCardPredicate(card,
                    gainEffect.filter(), context.source().getCard().getId(), gameData, located.ownerId())) {
                continue;
            }
            String link = "gained:" + card.getId();
            for (ActivatedAbility ability : ManaAbilities.withIntrinsic(card)) {
                ActivatedAbility gainedAbility = borrowedAbility(ability, link);
                accumulator.addActivatedAbility(gainEffect.oncePerTurn()
                        ? gainedAbility.withMaxActivationsPerTurn(1)
                        : gainedAbility);
            }
        }
    }

    /**
     * {@code ability} of a card the borrower doesn't have the other abilities of, as the borrower has it. A pair of
     * linked abilities gained together stays linked on the borrower, to each other only (CR 607.5): their exile tracks
     * under {@code link}. An imprint exile in it is not the borrower's imprint, so the borrower's own "cards exiled
     * with it" don't count what it exiles (CR 607.2a). "The exiled card" of the lender's imprint, without an exile
     * cost in the same ability to link it to, refers to no card on the borrower (CR 607.1): with "X is the mana value
     * of that card" X is undefined, so the ability can't be activated (Prototype Portal's ruling); otherwise that part
     * does nothing (Mimic Vat, Isochron Scepter).
     */
    static ActivatedAbility borrowedAbility(ActivatedAbility ability, String link) {
        boolean linkedToOwnExileCost = ability.getEffects().stream().anyMatch(effect ->
                effect instanceof ExileCardFromGraveyardCost cost && cost.imprintOnSource());
        boolean undefinedX = false;
        List<CardEffect> gainedEffects = new ArrayList<>();
        for (CardEffect effect : ability.getEffects()) {
            if (!linkedToOwnExileCost && effect instanceof ImprintedCardXCostEffect imprinted) {
                undefinedX |= imprinted.requiresImprintedXCost();
                continue;
            }
            gainedEffects.add(GainAbilitiesOfLastChosenExiledCardSelfEffectHandler.linkGainedEffect(effect, link));
        }
        ActivatedAbility gained = ability.withEffects(gainedEffects);
        // A condition of no alternatives is never met.
        return undefinedX
                ? gained.withActivationCondition(new AnyOf(List.of()), "No card was exiled with its linked ability")
                : gained;
    }

    /**
     * The cards {@code cards} names for a static ability of {@code source} that lend it their abilities: a card exiled
     * face down has none (CR 406.3a), nor has a graveyard card while graveyard cards have lost all abilities.
     */
    static List<LocatedCard> lendingCards(GameData gameData, GameQueryService gameQueryService,
                                          PredicateEvaluationService predicateEvaluationService, Permanent source,
                                          UUID controllerId, CardSubject cards) {
        List<LocatedCard> named = SubjectResolutionService.namedCards(gameData, source, controllerId, cards,
                (card, ownerId, filter) -> predicateEvaluationService.matchesCardPredicate(
                        card, filter, source.getCard().getId(), gameData, ownerId));
        boolean graveyardsLostAbilities = named.stream().anyMatch(located -> located.zone() == Zone.GRAVEYARD)
                && gameQueryService.graveyardCardsHaveLostAllAbilities(gameData);
        return named.stream()
                .filter(located -> !(located.zone() == Zone.GRAVEYARD && graveyardsLostAbilities))
                .filter(located -> {
                    if (located.zone() != Zone.EXILE) {
                        return true;
                    }
                    ExiledCardEntry exiled = gameData.findExiledCard(located.card().getId());
                    return exiled == null || !exiled.faceDown();
                })
                .toList();
    }
}
