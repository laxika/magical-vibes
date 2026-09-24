package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CopyPermanentOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTiming;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "1325")
public class EstridsInvocation extends Card {

    public EstridsInvocation() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CopyPermanentOnEnterEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "enchantment you control",
                Set.of(),
                Map.of(EffectSlot.UPKEEP_TRIGGERED, List.of(
                        new MayEffect(
                                new FlickerEffect(
                                        FlickerScope.SELF,
                                        null,
                                        ReturnTiming.IMMEDIATE,
                                        TurnStep.END_STEP,
                                        false,
                                        null,
                                        null,
                                        0,
                                        false,
                                        false
                                ),
                                "Exile this enchantment?"
                        )
                ))
        ));
    }
}
