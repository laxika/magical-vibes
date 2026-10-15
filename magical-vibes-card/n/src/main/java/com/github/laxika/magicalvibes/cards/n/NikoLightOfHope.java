package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DSK", collectorNumber = "224")
public class NikoLightOfHope extends Card {

    private static final PermanentPredicate NONLEGENDARY_CREATURE_YOU_CONTROL = new PermanentAllOfPredicate(
            List.of(
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentIsCreaturePredicate(),
                    new PermanentNotPredicate(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY))));

    private static final PermanentPredicate SHARDS_YOU_CONTROL = new PermanentAllOfPredicate(
            List.of(
                    new PermanentControlledBySourceControllerPredicate(),
                    new PermanentHasSubtypePredicate(CardSubtype.SHARD)));

    public NikoLightOfHope() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, shardToken(new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        FlickerEffect.exileTargetReturnAtEndStep(),
                        EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect
                                .untilNextEndStep(NONLEGENDARY_CREATURE_YOU_CONTROL, SHARDS_YOU_CONTROL)),
                "{2}, {T}: Exile target nonlegendary creature you control. Shards you control become copies "
                        + "of it until the next end step. Return it to the battlefield under its owner's control "
                        + "at the beginning of the next end step.",
                new PermanentPredicateTargetFilter(
                        NONLEGENDARY_CREATURE_YOU_CONTROL,
                        "Target must be a nonlegendary creature you control.")));
    }

    private static CreateTokenEffect shardToken(DynamicAmount amount) {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                amount,
                "Shard",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.SHARD),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(),
                List.of(new ActivatedAbility(
                        false,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new ScryEffect(1), new DrawCardEffect(1)),
                        "{2}, Sacrifice this token: Scry 1, then draw a card.")),
                false,
                false,
                false,
                0,
                Set.of());
    }
}
