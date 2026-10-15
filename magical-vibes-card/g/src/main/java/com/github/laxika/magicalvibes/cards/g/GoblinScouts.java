package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MIR", collectorNumber = "178")
public class GoblinScouts extends Card {

    public GoblinScouts() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                3, "Goblin Scout", 1, 1, CardColor.RED,
                List.of(CardSubtype.GOBLIN, CardSubtype.SCOUT),
                Set.of(Keyword.MOUNTAINWALK), Set.of()));
    }
}
