package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "156")
public class SwordOfTheParuns extends Card {

    public SwordOfTheParuns() {
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsTappedPredicate(),
                new StaticBoostEffect(2, 0, GrantScope.OWN_TAPPED_CREATURES),
                null));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentIsTappedPredicate()),
                new StaticBoostEffect(0, 2, GrantScope.OWN_UNTAPPED_CREATURES),
                null));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new MayEffect(
                        new ChooseOneEffect(List.of(
                                new ChooseOneEffect.ChooseOneOption(
                                        "Tap equipped creature",
                                        new TapPermanentsEffect(TapUntapScope.ENCHANTED)),
                                new ChooseOneEffect.ChooseOneOption(
                                        "Untap equipped creature",
                                        new UntapPermanentsEffect(TapUntapScope.ENCHANTED))
                        )),
                        "Tap or untap equipped creature?"
                )),
                "{3}: You may tap or untap equipped creature."
        ));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
