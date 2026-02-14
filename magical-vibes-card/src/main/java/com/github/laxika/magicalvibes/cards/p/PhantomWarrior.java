package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

import java.util.List;

public class PhantomWarrior extends Card {

    public PhantomWarrior() {
        super("Phantom Warrior", CardType.CREATURE, "{1}{U}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.ILLUSION, CardSubtype.WARRIOR));
        setCardText("Phantom Warrior can't be blocked.");
        setPower(2);
        setToughness(2);
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
