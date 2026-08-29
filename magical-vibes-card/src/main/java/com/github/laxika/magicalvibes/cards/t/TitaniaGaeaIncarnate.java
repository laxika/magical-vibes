package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BRO", collectorNumber = "256b")
public class TitaniaGaeaIncarnate extends Card {

    public TitaniaGaeaIncarnate() {
        PermanentCount landsYouControl = new PermanentCount(
                new PermanentIsLandPredicate(), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(landsYouControl, landsYouControl));

        // When Titania enters, return all land cards from your graveyard to the battlefield tapped.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardTypePredicate(CardType.LAND))
                .returnAll(true)
                .enterTapped(true)
                .build());

        // {3}{G}: Put four +1/+1 counters on target land you control. It becomes a 0/0 Elemental
        // creature with haste. It's still a land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 4),
                        new AnimatePermanentsEffect(
                                0, 0, List.of(CardSubtype.ELEMENTAL), Set.of(Keyword.HASTE),
                                null, Set.of(CardType.CREATURE), GrantScope.TARGET, EffectDuration.PERMANENT)),
                "{3}{G}: Put four +1/+1 counters on target land you control. It becomes a 0/0 Elemental "
                        + "creature with haste. It's still a land.",
                TargetFilters.landYouControl()
        ));
    }
}
