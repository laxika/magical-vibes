package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DGM", collectorNumber = "81")
public class LegionsInitiative extends Card {

    public LegionsInitiative() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.RED))));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 1, GrantScope.OWN_CREATURES,
                new PermanentColorInPredicate(Set.of(CardColor.WHITE))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}{W}",
                List.of(new ExileSelfCost(),
                        FlickerEffect.exileControllersPermanentsReturnAtStepWithHaste(
                                new PermanentIsCreaturePredicate(),
                                TurnStep.BEGINNING_OF_COMBAT)),
                "{R}{W}, Exile Legion's Initiative: Exile all creatures you control. At the beginning of the next combat, return those cards to the battlefield under their owner's control and those creatures gain haste until end of turn."
        ));
    }
}
