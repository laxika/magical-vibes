package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentCanBecomeTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;
import java.util.Set;

@CardRegistration(set = "SNC", collectorNumber = "56")
public class ReservoirKraken extends Card {

    public ReservoirKraken() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.ANY),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceUntapped()),
                MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT,
                        new TapPermanentsEffect(Subjects.controlledBy(Subjects.acceptedPlayers(),
                                new PermanentAllOfPredicate(List.of(new PermanentIsCreaturePredicate(),
                                        new PermanentCanBecomeTappedPredicate()))), null, 1),
                        "Tap an untapped creature you control? If a player does, Reservoir Kraken becomes tapped"
                                + " and its controller creates a 1/1 Fish.",
                        SequenceEffect.of(new TapPermanentsEffect(Subjects.source()), fishToken()), null));
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), UnlessPaysEffect.counterUnlessPays(2));
    }

    private static CreateTokenEffect fishToken() {
        return new CreateTokenEffect(
                1,
                "Fish",
                1,
                1,
                CardColor.BLUE,
                List.of(CardSubtype.FISH),
                Set.of(),
                Set.of(),
                List.of(StaticAbility.of(new CantBeBlockedEffect())));
    }
}
