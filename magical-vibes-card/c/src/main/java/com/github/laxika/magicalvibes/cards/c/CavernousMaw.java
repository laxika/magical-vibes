package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlledOtherPermanentsPlusGraveyardCardsAtLeast;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LCI", collectorNumber = "270")
public class CavernousMaw extends Card {

    public CavernousMaw() {
        // {T}: Add {C}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
        // {2}: This land becomes a 3/3 Elemental creature until end of turn. It's still a Cave land.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new AnimatePermanentsEffect(3, 3, List.of(CardSubtype.ELEMENTAL), Set.of())),
                "{2}: This land becomes a 3/3 Elemental creature until end of turn. It's still a Cave land. Activate only if the number of other Caves you control plus the number of Cave cards in your graveyard is three or greater."
        ).withActivationCondition(
                new ControlledOtherPermanentsPlusGraveyardCardsAtLeast(
                        3,
                        new PermanentHasSubtypePredicate(CardSubtype.CAVE),
                        new CardSubtypePredicate(CardSubtype.CAVE)),
                "Activate only if the number of other Caves you control plus the number of Cave cards in your graveyard is three or greater."
        ));
    }
}
