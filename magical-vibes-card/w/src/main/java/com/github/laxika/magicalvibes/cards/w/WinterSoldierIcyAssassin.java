package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.amount.AttachmentsOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AttachOneOfControlledEquipmentToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;

import java.util.List;

@CardRegistration(set = "MSH", collectorNumber = "239")
public class WinterSoldierIcyAssassin extends Card {

    public WinterSoldierIcyAssassin() {
        addEffect(EffectSlot.STATIC,
                new BoostSelfEffect(new Scaled(new AttachmentsOnSource(false, true), 2), new Fixed(0)));

        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{B}",
                List.of(
                        ReturnCardFromGraveyardEffect.builder()
                                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                                .filter(new CardIsSelfPredicate())
                                .returnAll(true)
                                .enterWithCounter(CounterType.FINALITY)
                                .enterWithCounterCount(1)
                                .build(),
                        new MayEffect(
                                new AttachOneOfControlledEquipmentToSourceEffect(),
                                "Attach an Equipment you control to Winter Soldier?")),
                "{3}{W}{B}: Return this card from your graveyard to the battlefield with a finality counter on it. "
                        + "Then you may attach an Equipment you control to it."
        ));
    }
}
