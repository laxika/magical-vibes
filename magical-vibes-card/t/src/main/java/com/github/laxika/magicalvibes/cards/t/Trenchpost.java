package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M3C", collectorNumber = "83")
@CardRegistration(set = "M3C", collectorNumber = "135")
public class Trenchpost extends Card {

    public Trenchpost() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(new MillEffect(
                        new PermanentCount(
                                new PermanentHasSubtypePredicate(CardSubtype.LOCUS),
                                CountScope.CONTROLLER),
                        MillRecipient.TARGET_PLAYER)),
                "{3}, {T}: Target player mills a card for each Locus you control."
        ));
    }
}
