package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YBRO", collectorNumber = "1")
public class ByElspethsCommand extends Card {

    private static final String TARGET_SOLDIER_MODE =
            "Up to one target Soldier perpetually gets +1/+1 and gains flying.";
    private static final String HAND_SOLDIER_MODE =
            "Choose a Soldier card in your hand. It perpetually gets +1/+1 and gains vigilance.";
    private static final String TOKEN_MODE =
            "Create a 1/1 colorless Soldier artifact creature token.";

    public ByElspethsCommand() {
        PermanentPredicate soldierPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSubtypePredicate(CardSubtype.SOLDIER)));
        PermanentPredicateTargetFilter soldierTarget = new PermanentPredicateTargetFilter(
                soldierPermanent, "Target must be a Soldier");
        CardPredicate soldierCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardSubtypePredicate(CardSubtype.SOLDIER)));

        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseModeNotYetChosenThisTurnEffect(List.of(
                        new ChooseOneEffect.ChooseOneOption(
                                TARGET_SOLDIER_MODE,
                                List.of(
                                        new PerpetuallyBoostTargetCreatureEffect(1, 1, soldierPermanent),
                                        new PerpetuallyGrantKeywordsToTargetCreatureEffect(
                                                Set.of(Keyword.FLYING), soldierPermanent)),
                                soldierTarget, null, 0, 1, false, null),
                        new ChooseOneEffect.ChooseOneOption(
                                HAND_SOLDIER_MODE,
                                new PerpetuallyBoostCreatureCardInHandEffect(
                                        1, 1, Set.of(Keyword.VIGILANCE), soldierCard)),
                        new ChooseOneEffect.ChooseOneOption(
                                TOKEN_MODE,
                                new CreateTokenEffect(1, "Soldier", 1, 1, null,
                                        List.of(CardSubtype.SOLDIER), Set.of(), Set.of(CardType.ARTIFACT))))));
    }
}
