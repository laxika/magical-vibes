package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "32")
@CardRegistration(set = "LEG", collectorNumber = "31")
public class PresenceOfTheMaster extends Card {

    public PresenceOfTheMaster() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardTypePredicate(CardType.ENCHANTMENT),
                List.of(new CounterSpellEffect())));
    }
}
