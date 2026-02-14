package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutAuraFromHandOntoSelfEffect;

import java.util.List;

public class AcademyResearchers extends Card {

    public AcademyResearchers() {
        super("Academy Researchers", CardType.CREATURE, "{1}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.WIZARD));
        setCardText("When Academy Researchers enters the battlefield, you may put an Aura card from your hand onto the battlefield attached to Academy Researchers.");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(new PutAuraFromHandOntoSelfEffect(), "Put an Aura from your hand onto the battlefield?"));
    }
}
