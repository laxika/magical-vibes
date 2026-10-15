package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "270")
public class NissaNaturesArtisan extends Card {

    public NissaNaturesArtisan() {
        addActivatedAbility(new ActivatedAbility(
                +3,
                List.of(new GainLifeEffect(3)),
                "+3: You gain 3 life."
        ));

        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new LookAtTopCardsEffect(
                        new Fixed(2), new Fixed(2), new CardTypePredicate(CardType.LAND),
                        LookDestination.HAND, true, LibrarySearchDestination.BATTLEFIELD, false,
                        false, null, null, false, 0, true, false, false, false, 0)),
                "\u22124: Reveal the top two cards of your library. Put all land cards from among them "
                        + "onto the battlefield and the rest into your hand."
        ));

        addActivatedAbility(new ActivatedAbility(
                -12,
                List.of(
                        new BoostAllOwnCreaturesEffect(5, 5),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.OWN_CREATURES)
                ),
                "\u221212: Creatures you control get +5/+5 and gain trample until end of turn."
        ));
    }
}
