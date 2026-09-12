package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.GreatestManaValueAmongControlled;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "VMA", collectorNumber = "82")
public class MuzzioVisionaryArchitect extends Card {

    public MuzzioVisionaryArchitect() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{U}",
                List.of(new LookAtTopCardsEffect(
                        new GreatestManaValueAmongControlled(new PermanentIsArtifactPredicate()),
                        new Fixed(1),
                        new CardTypePredicate(CardType.ARTIFACT),
                        LookDestination.BOTTOM_OF_LIBRARY,
                        false,
                        LibrarySearchDestination.BATTLEFIELD,
                        true)),
                "{3}{U}, {T}: Look at the top X cards of your library, where X is the greatest mana "
                        + "value among artifacts you control. You may put an artifact card from among "
                        + "them onto the battlefield. Put the rest on the bottom of your library in any order."
        ));
    }
}
