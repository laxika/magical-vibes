package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasChosenSpellColorPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.ClearChosenColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorAtResolutionEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "244")
public class DromarTheBanisher extends Card {

    public DromarTheBanisher() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                UnlessPaysEffect.youMayPay("{2}{U}", SequenceEffect.of(
                                new ChooseColorAtResolutionEffect(),
                                ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(), new PermanentHasChosenSpellColorPredicate()))),
                                new ClearChosenColorEffect())));
    }
}
