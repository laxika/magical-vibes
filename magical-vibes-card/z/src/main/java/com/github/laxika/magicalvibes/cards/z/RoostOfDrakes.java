package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.KickedSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ZNR", collectorNumber = "74")
public class RoostOfDrakes extends Card {

    private static final CreateTokenEffect DRAKE_TOKEN = new CreateTokenEffect(
            "Drake", 2, 2, CardColor.BLUE, List.of(CardSubtype.DRAKE), Set.of(Keyword.FLYING), Set.of());

    public RoostOfDrakes() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}{U}"));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(new Kicked(), DRAKE_TOKEN));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new KickedSpellCastTriggerEffect(List.of(DRAKE_TOKEN)));
    }
}
