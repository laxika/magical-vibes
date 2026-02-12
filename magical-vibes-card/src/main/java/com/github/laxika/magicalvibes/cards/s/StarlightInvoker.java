package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

public class StarlightInvoker extends Card {

    public StarlightInvoker() {
        super("Starlight Invoker", CardType.CREATURE, "{1}{W}", CardColor.WHITE);

        setSubtypes(List.of(CardSubtype.HUMAN, CardSubtype.CLERIC, CardSubtype.MUTANT));
        setCardText("{7}{W}: You gain 5 life.");
        setPower(1);
        setToughness(3);
        addEffect(EffectSlot.MANA_ACTIVATED_ABILITY, new GainLifeEffect(5));
        setManaActivatedAbilityCost("{7}{W}");
    }
}
