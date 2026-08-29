package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "199")
@CardRegistration(set = "MKM", collectorNumber = "415")
public class DragTheCanal extends Card {

    public DragTheCanal() {
        addEffect(EffectSlot.SPELL,
                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLUE), List.of(CardSubtype.DETECTIVE)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new Morbid(),
                SequenceEffect.of(
                        new GainLifeEffect(2),
                        new SurveilEffect(2),
                        CreateTokenEffect.ofClueToken(1))));
    }
}
