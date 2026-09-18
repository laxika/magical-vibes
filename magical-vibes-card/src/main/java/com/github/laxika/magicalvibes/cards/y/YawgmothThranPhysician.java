package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromSubtypesEffect;
import java.util.Set;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MH1", collectorNumber = "116")
@CardRegistration(set = "TSR", collectorNumber = "336")
@CardRegistration(set = "DMR", collectorNumber = "110")
@CardRegistration(set = "FCA", collectorNumber = "11")
public class YawgmothThranPhysician extends Card {

    public YawgmothThranPhysician() {
        addEffect(EffectSlot.STATIC, new ProtectionFromSubtypesEffect(Set.of(CardSubtype.HUMAN)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayLifeCost(1),
                        new SacrificeCreatureCost(false, false, false, true),
                        new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE),
                        new DrawCardEffect(1)
                ),
                "Pay 1 life, Sacrifice another creature: Put a -1/-1 counter on up to one target creature and draw a card.",
                null,
                null,
                null,
                null,
                List.<TargetFilter>of(TargetFilters.creature()),
                0,
                1
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new DiscardCardTypeCost(null, "card"), new ProliferateEffect()),
                "{B}{B}, Discard a card: Proliferate."
        ));
    }
}
