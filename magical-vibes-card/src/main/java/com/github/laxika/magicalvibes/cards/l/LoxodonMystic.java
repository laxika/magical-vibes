package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapTargetCreatureEffect;

import java.util.List;

public class LoxodonMystic extends Card {

    public LoxodonMystic() {
        super("Loxodon Mystic", CardType.CREATURE, "{3}{W}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.ELEPHANT, CardSubtype.CLERIC));
        setCardText("{W}, {T}: Tap target creature.");
        setPower(3);
        setToughness(3);
        setNeedsTarget(true);
        addEffect(EffectSlot.TAP_ACTIVATED_ABILITY, new TapTargetCreatureEffect());
        setTapActivatedAbilityCost("{W}");
    }
}
