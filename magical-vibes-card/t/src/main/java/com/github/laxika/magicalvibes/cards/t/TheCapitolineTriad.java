package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsHistoricPredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "1")
@CardRegistration(set = "ACR", collectorNumber = "117")
public class TheCapitolineTriad extends Card {

    private static final CardIsHistoricPredicate HISTORIC = new CardIsHistoricPredicate();
    private static final String EMBLEM_TEXT = "Creatures you control have base power and toughness 9/9.";

    public TheCapitolineTriad() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                HISTORIC,
                new CardsInGraveyard(HISTORIC, CountScope.CONTROLLER),
                CostModificationScope.SELF));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        CollectEvidenceCost.forManaValueWithoutCollectingEvidence(30, HISTORIC),
                        new CreateEmblemEffect(List.of(new SetBasePowerToughnessEffect(
                                9, 9, GrantScope.ALL_OWN_CREATURES, EffectDuration.CONTINUOUS)), EMBLEM_TEXT)
                ),
                "Exile any number of historic cards from your graveyard with total mana value 30 or greater: "
                        + "You get an emblem with \"" + EMBLEM_TEXT + "\"."
        ));
    }
}
