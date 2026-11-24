package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CardSubject;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
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
        boolean exiledWithSource = gainEffect.cards() instanceof CardSubject.ExiledWithSource;
        for (LocatedCard located : lendingCards(gameData, gameQueryService, predicateEvaluationService,
                context.source(), context.sourceControllerId(), gainEffect.cards())) {
            Card card = located.card();
            if (gainEffect.filter() != null && !predicateEvaluationService.matchesCardPredicate(card,
                    gainEffect.filter(), context.source().getCard().getId(), gameData, located.ownerId())) {
                continue;
            }
            for (ActivatedAbility ability : ManaAbilities.withIntrinsic(card)) {
                // "X is the mana value of the exiled card" refers to a card exiled by the ability's own
                // source, so a copy borrowed from a card exiled with this has no such card (Prototype Portal)
                // and is left out.
                if (exiledWithSource && ability.getEffects().stream().anyMatch(e -> e instanceof ImprintedCardXCostEffect imprinted
                        && imprinted.requiresImprintedXCost())) {
                    continue;
                }
                List<CardEffect> gainedEffects = ability.getEffects().stream().map(gainedEffect ->
                        gainedEffect instanceof ExileTargetPermanentAndTrackWithSourceEffect exile
                                && exile.abilityLink() != null
                                ? (CardEffect) new ExileTargetPermanentAndTrackWithSourceEffect(
                                        "gained:" + card.getId() + ":" + exile.abilityLink())
                                : gainedEffect).toList();
                ActivatedAbility gainedAbility = ability.withEffects(gainedEffects);
                accumulator.addActivatedAbility(gainEffect.oncePerTurn()
                        ? gainedAbility.withMaxActivationsPerTurn(1)
                        : gainedAbility);
            }
        }
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
