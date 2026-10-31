package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfDefendingPlayerLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToOwnCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasActivatedAbilityWithEffectsPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasStaticEffectPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedIfDefenderControlsMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;
import java.util.stream.Stream;

@CardRegistration(set = "MB2", collectorNumber = "315")
public class OddricLunarMarquis extends Card {

    private static final List<Keyword> SHARED_KEYWORDS = List.of(
            Keyword.BANDING,
            Keyword.CHANGELING,
            Keyword.DEVOID,
            Keyword.FEAR,
            Keyword.FLANKING,
            Keyword.HORSEMANSHIP,
            Keyword.INGEST,
            Keyword.INTIMIDATE,
            Keyword.FORESTWALK,
            Keyword.MOUNTAINWALK,
            Keyword.ISLANDWALK,
            Keyword.SWAMPWALK,
            Keyword.PLAINSWALK,
            Keyword.DESERTWALK,
            Keyword.SHROUD,
            Keyword.TANTRUM,
            Keyword.WITHER
    );

    private static final CardEffect NONBASIC_LANDWALK = new CantBeBlockedIfDefenderControlsMatchingPermanentEffect(
            new PermanentAllOfPredicate(List.of(
                    new PermanentIsLandPredicate(),
                    new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.BASIC)))),
            true);

    private static final List<CardEffect> COLORLESS_MANA_SACRIFICE_EFFECTS =
            List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.COLORLESS));

    public OddricLunarMarquis() {
        ActivatedAbility colorlessManaAbility = new ActivatedAbility(
                false,
                null,
                COLORLESS_MANA_SACRIFICE_EFFECTS,
                "Sacrifice this creature: Add {C}."
        );
        List<CardEffect> sharedEffects = Stream.concat(
                SHARED_KEYWORDS.stream().map(keyword -> (CardEffect) conditionalSharedEffect(
                        keyword,
                        new GrantKeywordEffect(keyword, GrantScope.ALL_OWN_CREATURES))),
                Stream.<CardEffect>of(
                        conditionalSharedEffect(Keyword.INGEST,
                                new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(
                                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                        new ExileTopCardsOfDefendingPlayerLibraryEffect(1))),
                        new ConditionalEffect(
                                sharesNonbasicLandwalk(),
                                new GrantEffectToOwnCreaturesUntilEndOfTurnEffect(StaticAbility.of(NONBASIC_LANDWALK))),
                        new ConditionalEffect(
                                sharesManaAbility(),
                                new GrantActivatedAbilityEffect(
                                        colorlessManaAbility,
                                        GrantScope.ALL_OWN_CREATURES,
                                        null,
                                        EffectDuration.UNTIL_END_OF_TURN,
                                        null))
                )
        ).toList();

        addEffect(EffectSlot.EACH_BEGINNING_OF_COMBAT_TRIGGERED,
                new SequenceEffect(sharedEffects));
    }

    private static ConditionalEffect conditionalSharedEffect(Keyword keyword, CardEffect effect) {
        return new ConditionalEffect(shares(keyword), effect);
    }

    private static ControlsPermanent shares(Keyword keyword) {
        return new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasKeywordPredicate(keyword)
        )));
    }

    private static ControlsPermanent sharesNonbasicLandwalk() {
        return new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasStaticEffectPredicate(NONBASIC_LANDWALK)
        )));
    }

    private static ControlsPermanent sharesManaAbility() {
        return new ControlsPermanent(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasActivatedAbilityWithEffectsPredicate(COLORLESS_MANA_SACRIFICE_EFFECTS)
        )));
    }
}
