package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureCreateTokensEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "CMD", collectorNumber = "103")
public class SyphonFlesh extends Card {

    public SyphonFlesh() {
        addEffect(EffectSlot.SPELL, new EachOpponentSacrificesCreatureCreateTokensEffect(
                new CreateTokenEffect("Zombie", 2, 2, CardColor.BLACK,
                        List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())));
    }
}
