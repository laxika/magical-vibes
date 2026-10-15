package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "281")
@CardRegistration(set = "MB2", collectorNumber = "517")
public class JeskaiBaller extends Card {

    public JeskaiBaller() {
        addEffect(EffectSlot.ON_SELF_CAST, new CreateTokenEffect(
                "Athlete", 1, 1, CardColor.WHITE, List.of(CardSubtype.ATHLETE), Set.of(), Set.of()));
    }
}
