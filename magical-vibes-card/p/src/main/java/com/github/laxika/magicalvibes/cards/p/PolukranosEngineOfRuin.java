package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

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
    private static final CreateTokenEffect CREATE_HYDRAS =
            REACH_HYDRA.withSimultaneousTokens(List.of(LIFELINK_HYDRA));

    public PolukranosEngineOfRuin() {
        addTrigger(Triggers.creatureDies(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.HYDRA),
                                new PermanentNotPredicate(new PermanentIsTokenPredicate()))),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                CREATE_HYDRAS);
    }
}
