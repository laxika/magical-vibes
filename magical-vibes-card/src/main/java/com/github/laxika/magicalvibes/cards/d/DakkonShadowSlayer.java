package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOrGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "192")
@CardRegistration(set = "MH2", collectorNumber = "304")
public class DakkonShadowSlayer extends Card {

    public DakkonShadowSlayer() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(),
                CountScope.CONTROLLER);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.LOYALTY, landsYouControl));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SurveilEffect(2)),
                "+1: Surveil 2."
        ));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new ExileTargetPermanentEffect(new PermanentIsCreaturePredicate())),
                "−3: Exile target creature.",
                TargetFilters.creature()
        ));

        addActivatedAbility(new ActivatedAbility(
                -6,
                List.of(new PutCardFromHandOrGraveyardOntoBattlefieldEffect(
                        new CardTypePredicate(CardType.ARTIFACT), "artifact")),
                "−6: You may put an artifact card from your hand or graveyard onto the battlefield."
        ));
    }
}
