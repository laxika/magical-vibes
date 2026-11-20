package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TDM", collectorNumber = "252")
public class CoriMountainMonastery extends Card {

    public CoriMountainMonastery() {
        addStaticEffect(new ConditionalReplacementEffect(
                new ControlsPermanentCountAtMost(0,
                        new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.PLAINS, CardSubtype.ISLAND))),
                new EntersTappedEffect()));

        // {T}: Add {R}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.RED));

        // {3}{R}, {T}: Exile the top card of your library. Until the end of your next turn, you may
        // play that card.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}{R}",
                List.of(new ExileTopCardMayPlayThisTurnEffect(1, false).until(Duration.UNTIL_END_OF_YOUR_NEXT_TURN)),
                "{3}{R}, {T}: Exile the top card of your library. Until the end of your next turn, "
                        + "you may play that card."));
    }
}
