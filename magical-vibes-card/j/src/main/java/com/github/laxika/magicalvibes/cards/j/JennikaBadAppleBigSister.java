package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "10")
@CardRegistration(set = "TMT", collectorNumber = "203")
public class JennikaBadAppleBigSister extends Card {

    public JennikaBadAppleBigSister() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new CreateTokenEffect(
                "Mutant", 2, 2, CardColor.RED, List.of(CardSubtype.MUTANT), Set.of(), Set.of()));
    }
}
