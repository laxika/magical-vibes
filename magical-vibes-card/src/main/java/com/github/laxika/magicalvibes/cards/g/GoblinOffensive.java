package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "USG", collectorNumber = "192")
@CardRegistration(set = "ATH", collectorNumber = "37")
public class GoblinOffensive extends Card {

    public GoblinOffensive() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(new XValue(), "Goblin", 1, 1,
                CardColor.RED, List.of(CardSubtype.GOBLIN), Set.of(), Set.of()));
    }
}
