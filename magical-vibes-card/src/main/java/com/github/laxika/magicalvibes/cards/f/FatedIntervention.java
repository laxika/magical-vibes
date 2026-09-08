package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "121")
public class FatedIntervention extends Card {

    public FatedIntervention() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(2, "Centaur", 3, 3, CardColor.GREEN,
                List.of(CardSubtype.CENTAUR), Set.of(), Set.of(CardType.ENCHANTMENT)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new ControllerTurn(), new ScryEffect(2)));
    }
}
