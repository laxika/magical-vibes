package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreatedTokensFightDifferentOpponentCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "36")
public class EzurisPredation extends Card {

    public EzurisPredation() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.OPPONENTS),
                "Phyrexian Beast", 4, 4, CardColor.GREEN,
                List.of(CardSubtype.PHYREXIAN, CardSubtype.BEAST), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new CreatedTokensFightDifferentOpponentCreaturesEffect());
    }
}
