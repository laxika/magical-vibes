package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "43")
public class DaxosTheReturned extends Card {

    public DaxosTheReturned() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardTypePredicate(CardType.ENCHANTMENT),
                        List.of(new ExperienceCountersEffect(1))));

        CreateTokenEffect spirit = new CreateTokenEffect(
                CardType.CREATURE, 1, "Spirit", 0, 0,
                CardColor.WHITE, Set.of(CardColor.WHITE, CardColor.BLACK),
                List.of(CardSubtype.SPIRIT), Set.of(), Set.of(CardType.ENCHANTMENT),
                false, false,
                Map.of(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(
                        new ControllerExperienceCounters(), new ControllerExperienceCounters())),
                List.of(), false, false, false, 0, Set.of(), Set.of());
        addActivatedAbility(new ActivatedAbility(false, "{1}{W}{B}", List.of(spirit),
                "{1}{W}{B}: Create a white and black Spirit enchantment creature token. It has \"This creature's power and toughness are each equal to the number of experience counters you have.\""));
    }
}
