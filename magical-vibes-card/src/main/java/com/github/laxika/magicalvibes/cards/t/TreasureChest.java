package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "252")
public class TreasureChest extends Card {

    public TreasureChest() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}",
                List.of(
                        new SacrificeSelfCost(),
                        new RollD20Effect(
                                new LoseLifeEffect(3),
                                CreateTokenEffect.ofTreasureToken(5),
                                SequenceEffect.of(new GainLifeEffect(3), new DrawCardEffect(3)),
                                SequenceEffect.of(
                                        new SearchLibraryEffect(null, LibrarySearchDestination.TOP_OF_LIBRARY),
                                        new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                                                new CardTypePredicate(CardType.ARTIFACT), false))
                        )
                ),
                "{4}, Sacrifice this artifact: Roll a d20."
        ));
    }
}
