package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CoronationOfTheWilds;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "340")
@CardRegistration(set = "MB2", collectorNumber = "577")
public class KeeperOfTheCrown extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("KeeperOfTheCrown", new OracleData(
                "Keeper of the Crown",
                CardType.CREATURE,
                Set.of(),
                "{2}{L}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(CardSubtype.HUMAN, CardSubtype.NOBLE),
                "(L can be paid with one mana from a legendary source.)\n"
                        + "Other legendary creatures you control get +1/+1 and have indestructible.",
                3,
                4,
                Set.of(),
                null,
                null,
                null));
    }

    public KeeperOfTheCrown() {
        setBackFaceCard(new CoronationOfTheWilds());
        addCastingOption(new AdventureCast("{2}{G}"));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                1,
                1,
                Set.of(Keyword.INDESTRUCTIBLE),
                GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)))));
    }

    @Override
    public String getBackFaceClassName() {
        return "CoronationOfTheWilds";
    }
}
