package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsEnchantment;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeEnchantmentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "22")
public class OpalAcrolith extends Card {

    public OpalAcrolith() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, SpellCastTriggerEffect.withIntervening(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new BecomeCreatureEffect(2, 4, CardSubtype.SOLDIER)),
                new SourceIsEnchantment()));

        addActivatedAbility(new ActivatedAbility(false, "{0}", List.of(new BecomeEnchantmentEffect()),
                "{0}: This permanent becomes an enchantment."));
    }
}
