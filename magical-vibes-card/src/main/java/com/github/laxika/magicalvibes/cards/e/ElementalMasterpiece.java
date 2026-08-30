package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "182")
public class ElementalMasterpiece extends Card {

    public ElementalMasterpiece() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Elemental", 4, 4, CardColor.BLUE,
                Set.of(CardColor.BLUE, CardColor.RED), List.of(CardSubtype.ELEMENTAL)));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{U/R}{U/R}",
                List.of(CreateTokenEffect.ofTreasureToken(1)),
                "{U/R}{U/R}, Discard this card: Create a Treasure token."));
    }
}
