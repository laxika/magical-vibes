package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.f.FullyGrownTreefolk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.OracleData;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MB2", collectorNumber = "344")
@CardRegistration(set = "MB2", collectorNumber = "582")
public class PlantASapling extends Card {

    /* The MB2 playtest printing is absent from the public oracle feeds. */
    static {
        Card.registerOracle("PlantASapling", new OracleData(
                "Plant a Sapling",
                CardType.SORCERY,
                Set.of(),
                "{G}",
                CardColor.GREEN,
                List.of(CardColor.GREEN),
                List.of(CardColor.GREEN),
                Set.of(),
                List.of(),
                "Search your library for a basic land card, reveal it, put it into your hand, then shuffle this spell "
                        + "into its owner's library transformed. (For playtesting purposes, put it in its sleeve upside down.)",
                null,
                null,
                Set.of(),
                null,
                null,
                null));
    }

    public PlantASapling() {
        setBackFaceCard(new FullyGrownTreefolk());

        addEffect(EffectSlot.SPELL, new SearchLibraryEffect(
                CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND, null, false));
        addEffect(EffectSlot.SPELL, new ShuffleIntoLibraryEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "FullyGrownTreefolk";
    }
}
