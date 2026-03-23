package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PowerToughnessEqualToControlledCreatureCountEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "17")
public class GeistHonoredMonk extends Card {

    public GeistHonoredMonk() {
        // Geist-Honored Monk's power and toughness are each equal to the number of creatures you control.
        addEffect(EffectSlot.STATIC, new PowerToughnessEqualToControlledCreatureCountEffect());

        // When Geist-Honored Monk enters the battlefield, create two 1/1 white Spirit creature tokens with flying.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                2, "Spirit", 1, 1, CardColor.WHITE, List.of(CardSubtype.SPIRIT), Set.of(Keyword.FLYING), Set.of()));
    }
}
