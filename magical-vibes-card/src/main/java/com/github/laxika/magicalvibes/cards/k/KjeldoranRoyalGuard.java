package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;

import java.util.List;

public class KjeldoranRoyalGuard extends Card {

    public KjeldoranRoyalGuard() {
        super("Kjeldoran Royal Guard", CardType.CREATURE, "{3}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER));
        setCardText("{T}: All combat damage that would be dealt to you by unblocked creatures this turn is dealt to Kjeldoran Royal Guard instead.");
        setPower(2);
        setToughness(5);
        addEffect(EffectSlot.TAP_ACTIVATED_ABILITY, new RedirectUnblockedCombatDamageToSelfEffect());
    }
}
