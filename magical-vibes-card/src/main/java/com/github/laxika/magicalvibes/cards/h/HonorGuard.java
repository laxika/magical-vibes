package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

public class HonorGuard extends Card {

    public HonorGuard() {
        super("Honor Guard", CardType.CREATURE, "{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        setCardText("{W}: Honor Guard gets +0/+1 until end of turn.");
        setPower(1);
        setToughness(1);
        setManaActivatedAbilityEffects(List.of(new BoostSelfEffect(0, 1)));
        setManaActivatedAbilityCost("{W}");
    }
}
