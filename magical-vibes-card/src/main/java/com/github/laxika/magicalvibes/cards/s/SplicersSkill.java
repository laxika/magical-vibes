package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SpliceEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH1", collectorNumber = "31")
public class SplicersSkill extends Card {

    public SplicersSkill() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                "Phyrexian Golem", 3, 3, null,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT)));
        addEffect(EffectSlot.STATIC, SpliceEffect.ontoInstantOrSorcery("{3}{W}"));
    }
}
