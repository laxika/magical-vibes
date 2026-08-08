package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.AllowCastMatchingCardsFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "106")
public class LilianaUntouchedByDeath extends Card {

    public LilianaUntouchedByDeath() {
        // +1: Mill three cards. If at least one Zombie card is milled this way, each opponent
        // loses 2 life and you gain 2 life.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new MillControllerThenIfMilledEffect(
                        3,
                        new CardSubtypePredicate(CardSubtype.ZOMBIE),
                        new SequenceEffect(List.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(2))))),
                "+1: Mill three cards. If at least one Zombie card is milled this way, "
                        + "each opponent loses 2 life and you gain 2 life."
        ));

        // −2: Target creature gets -X/-X until end of turn, where X is the number of Zombies you control.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new BoostTargetCreatureEffect(zombiesYouControlNegated(), zombiesYouControlNegated())),
                "−2: Target creature gets -X/-X until end of turn, where X is the number of Zombies you control.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsCreaturePredicate(),
                        "Target must be a creature")
        ));

        // −3: You may cast Zombie spells from your graveyard this turn.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new AllowCastMatchingCardsFromGraveyardThisTurnEffect(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE))),
                "−3: You may cast Zombie spells from your graveyard this turn."
        ));
    }

    private static Scaled zombiesYouControlNegated() {
        return new Scaled(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER),
                -1);
    }
}
