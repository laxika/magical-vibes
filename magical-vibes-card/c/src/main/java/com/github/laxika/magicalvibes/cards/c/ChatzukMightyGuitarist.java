package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesInAttackingBandsEffect;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "354")
@CardRegistration(set = "MB2", collectorNumber = "590")
public class ChatzukMightyGuitarist extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("ChatzukMightyGuitarist", new OracleData(
                "Chatzuk, Mighty Guitarist",
                CardType.CREATURE,
                Set.of(),
                "{1}{G}{W}",
                CardColor.WHITE,
                List.of(CardColor.WHITE, CardColor.GREEN),
                List.of(CardColor.WHITE, CardColor.GREEN),
                Set.of(CardSupertype.LEGENDARY),
                List.of(CardSubtype.HUMAN, CardSubtype.BARD),
                "Banding (Just ask around until you find someone who knows.)\n"
                        + "Creature spells you cast with banding cost {2} less to cast.\n"
                        + "Whenever two or more creatures you control attack in a band, each creature in that band gets +1/+1 until end of turn for each creature in that band.",
                2,
                2,
                Set.of(Keyword.BANDING),
                null,
                null,
                null));
    }

    public ChatzukMightyGuitarist() {
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardKeywordPredicate(Keyword.BANDING))),
                2,
                CostModificationScope.SELF));
        addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK, new BoostCreaturesInAttackingBandsEffect());
    }
}
