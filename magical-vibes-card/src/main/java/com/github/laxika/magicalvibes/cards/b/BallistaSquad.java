package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DealXDamageToTargetCreatureEffect;

import java.util.List;

public class BallistaSquad extends Card {

    public BallistaSquad() {
        super("Ballista Squad", CardType.CREATURE, "{3}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.REBEL));
        setCardText("{X}{W}, {T}: Ballista Squad deals X damage to target attacking or blocking creature.");
        setPower(2);
        setToughness(2);
        setTapActivatedAbilityEffects(List.of(new DealXDamageToTargetCreatureEffect()));
        setTapActivatedAbilityCost("{X}{W}");
    }
}
