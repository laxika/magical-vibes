package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;

import java.util.List;
import java.util.Set;

public class FogElemental extends Card {

    public FogElemental() {
        super("Fog Elemental", CardType.CREATURE, "{2}{U}", CardColor.BLUE);

        setSubtypes(List.of(CardSubtype.ELEMENTAL));
        setCardText("Flying\nWhen Fog Elemental attacks or blocks, sacrifice it at end of combat.");
        setKeywords(Set.of(Keyword.FLYING));
        setPower(4);
        setToughness(4);
        addEffect(EffectSlot.ON_ATTACK, new SacrificeAtEndOfCombatEffect());
        addEffect(EffectSlot.ON_BLOCK, new SacrificeAtEndOfCombatEffect());
    }
}
