package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "134")
public class HireACrew extends Card {

    public HireACrew() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                1, "Villain", 2, 1, CardColor.BLACK,
                List.of(CardSubtype.VILLAIN), Set.of(Keyword.MENACE), Set.of()));
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 0));
    }
}
