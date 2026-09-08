package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "JOU", collectorNumber = "96")
public class FlurryOfHorns extends Card {

    public FlurryOfHorns() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                2, "Minotaur", 2, 3, CardColor.RED,
                List.of(CardSubtype.MINOTAUR), Set.of(Keyword.HASTE), Set.of()));
    }
}
