package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "148")
public class FollowTheLumarets extends Card {

    public FollowTheLumarets() {
        // Look at the top four cards. Normally reveal one creature or land and put it into your
        // hand; Infusion — if you gained life this turn, instead reveal up to two. Rest go to the
        // bottom of the library.
        CardAnyOfPredicate creatureOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND)));

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GainedLifeThisTurn(),
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4, creatureOrLand),
                new LookAtTopCardsMayRevealByPredicatePutIntoHandRestOnBottomEffect(4, creatureOrLand, true, 2)));
    }
}
