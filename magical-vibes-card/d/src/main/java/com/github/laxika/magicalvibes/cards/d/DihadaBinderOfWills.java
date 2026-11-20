package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAllPermanentsMatchingEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DMC", collectorNumber = "1")
@CardRegistration(set = "DMC", collectorNumber = "49")
public class DihadaBinderOfWills extends Card {

    public DihadaBinderOfWills() {
        PermanentPredicate legendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new GrantKeywordEffect(
                        Set.of(Keyword.VIGILANCE, Keyword.LIFELINK, Keyword.INDESTRUCTIBLE),
                        Subjects.target(),
                        null,
                        Duration.UNTIL_YOUR_NEXT_TURN,
                        null)),
                "+2: Up to one target legendary creature gains vigilance, lifelink, and indestructible until your next turn.",
                new PermanentPredicateTargetFilter(
                        legendaryCreature, "Target must be a legendary creature"),
                +2,
                null,
                null,
                List.of(),
                0,
                1));

        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(
                        new LookAtTopCardsEffect(
                                new Fixed(4),
                                new Fixed(4),
                                new CardSupertypePredicate(CardSupertype.LEGENDARY),
                                LookDestination.GRAVEYARD,
                                true,
                                LibrarySearchDestination.HAND,
                                true,
                                false,
                                null,
                                null,
                                false).withRecordGraveyardCount(),
                        CreateTokenEffect.ofTreasureToken(new EventValue())),
                "−3: Reveal the top four cards of your library. Put any number of legendary cards from among them into your hand and the rest into your graveyard. Create a Treasure token for each card put into your graveyard this way."));

        PermanentPredicate nonlandPermanent = new PermanentNotPredicate(new PermanentIsLandPredicate());
        addActivatedAbility(new ActivatedAbility(
                -11,
                List.of(
                        new GainControlOfAllPermanentsMatchingEffect(nonlandPermanent, Duration.UNTIL_END_OF_TURN),
                        new UntapPermanentsEffect(Subjects.controlledBy(Subjects.you(), nonlandPermanent)),
                        new GrantKeywordEffect(Keyword.HASTE, Subjects.permanentsYouControl(), nonlandPermanent)),
                "−11: Gain control of all nonland permanents until end of turn. Untap them. They gain haste until end of turn."));
    }
}
