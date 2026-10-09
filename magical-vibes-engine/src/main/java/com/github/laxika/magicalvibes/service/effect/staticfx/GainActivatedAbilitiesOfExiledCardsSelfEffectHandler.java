package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfExiledCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GainActivatedAbilitiesOfExiledCardsSelfEffectHandler implements StaticEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;

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
        GainActivatedAbilitiesOfExiledCardsEffect gainEffect =
                (GainActivatedAbilitiesOfExiledCardsEffect) effect;
        List<Card> exiledCards = context.gameData().getCardsExiledByPermanent(context.sourceId());
        if (exiledCards.isEmpty()) return;
        for (Card card : exiledCards) {
            var exiledEntry = context.gameData().findExiledCard(card.getId());
            if (gainEffect.abilityLink() != null && (exiledEntry == null
                    || !gainEffect.abilityLink().equals(exiledEntry.abilityLink()))) {
                continue;
            }
            if (gainEffect.filter() != null && !predicateEvaluationService.matchesCardPredicate(
                    card, gainEffect.filter(), context.source().getCard().getId(), context.gameData(),
                    exiledEntry != null ? exiledEntry.ownerId() : null)) {
                continue;
            }
            for (var ability : card.getActivatedAbilities()) {
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
            List<CardEffect> onTapEffects = card.getEffects(EffectSlot.ON_TAP);
            if (!onTapEffects.isEmpty()) {
                ActivatedAbility manaAbility = new ActivatedAbility(
                        true, null, onTapEffects, "{T}: Add mana.");
                accumulator.addActivatedAbility(gainEffect.oncePerTurn()
                        ? manaAbility.withMaxActivationsPerTurn(1)
                        : manaAbility);
            }
        }
    }
}
