package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;
import java.util.Set;

public class PolukranosEngineOfRuin extends Card {

    private static final CreateTokenEffect REACH_HYDRA = new CreateTokenEffect(
            1,
            "Phyrexian Hydra",
            3,
            3,
            CardColor.GREEN,
            Set.of(CardColor.GREEN, CardColor.WHITE),
            List.of(CardSubtype.PHYREXIAN, CardSubtype.HYDRA),
            Set.of(Keyword.REACH),
            Set.of());
    private static final CreateTokenEffect LIFELINK_HYDRA = new CreateTokenEffect(
            1,
            "Phyrexian Hydra",
            3,
            3,
            CardColor.GREEN,
            Set.of(CardColor.GREEN, CardColor.WHITE),
            List.of(CardSubtype.PHYREXIAN, CardSubtype.HYDRA),
            Set.of(Keyword.LIFELINK),
            Set.of());
    private static final SequenceEffect CREATE_HYDRAS = SequenceEffect.of(REACH_HYDRA, LIFELINK_HYDRA);

    public PolukranosEngineOfRuin() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardAllOfPredicate(List.of(
                                new CardSubtypePredicate(CardSubtype.HYDRA),
                                new CardNotPredicate(new CardIsTokenPredicate()))),
                        CREATE_HYDRAS));
        addEffect(EffectSlot.ON_DEATH, CREATE_HYDRAS);
    }
}
