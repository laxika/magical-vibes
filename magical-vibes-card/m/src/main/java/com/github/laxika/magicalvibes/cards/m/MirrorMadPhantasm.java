package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "68")
public class MirrorMadPhantasm extends Card {

    public MirrorMadPhantasm() {
        addActivatedAbility(new ActivatedAbility(
                false, "{1}{U}",
                List.of(new ShuffleSelfIntoOwnerLibraryRevealUntilNameToBattlefieldEffect("Mirror-Mad Phantasm")),
                "{1}{U}: Mirror-Mad Phantasm's owner shuffles it into their library. If that player does, they reveal cards from the top of their library until a card named Mirror-Mad Phantasm is revealed. The player puts that card onto the battlefield and all other cards revealed this way into their graveyard."
        ));
    }
}
