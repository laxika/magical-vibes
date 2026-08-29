package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "13")
public class DescendUponTheSinful extends Card {

    public DescendUponTheSinful() {
        addEffect(EffectSlot.SPELL, new ExileAllPermanentsEffect(new PermanentIsCreaturePredicate()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new Delirium(),
                new CreateTokenEffect("Angel", 4, 4, CardColor.WHITE,
                        List.of(CardSubtype.ANGEL), Set.of(Keyword.FLYING), Set.of())
        ));
    }
}
