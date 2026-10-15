package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLE", collectorNumber = "246")
@CardRegistration(set = "TLE", collectorNumber = "287")
public class ZukoAvatarHunter extends Card {

    public ZukoAvatarHunter() {
        // Whenever you cast a red spell, create a 2/2 red Soldier creature token.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardColorPredicate(CardColor.RED),
                List.of(new CreateTokenEffect("Soldier", 2, 2, CardColor.RED,
                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of()))));
    }
}
