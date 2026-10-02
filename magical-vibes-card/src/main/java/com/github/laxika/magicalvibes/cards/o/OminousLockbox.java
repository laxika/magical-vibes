package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNumberOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsSourceChosenNumberPredicate;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "8")
public class OminousLockbox extends Card {

    public OminousLockbox() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseNumberOnEnterEffect(1, 20));
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new SpellCastTriggerEffect(
                null,
                List.of(new SacrificeSelfThenEffect(new CopyTriggeringSpellEffect())),
                new StackEntryManaValueEqualsSourceChosenNumberPredicate()));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect()),
                "{2}, Sacrifice Ominous Lockbox: Draw a card."
        ));
    }
}
