package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "EMN", collectorNumber = "153")
public class CropSigil extends Card {

    public CropSigil() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(new MillEffect(1, MillRecipient.CONTROLLER), "Mill a card?"));

        CardAnyOfPredicate creatureOrLand = new CardAnyOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardTypePredicate(CardType.LAND)));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{G}",
                List.of(new SacrificeSelfCost(), new ReturnTargetCardsFromGraveyardToHandEffect(creatureOrLand, 2)),
                "{2}{G}, Sacrifice this enchantment: Return up to one target creature card and up to one target land card from your graveyard to your hand. "
                        + "Activate only if there are four or more card types among cards in your graveyard.",
                List.of(new GraveyardCardPredicateTargetFilter(creatureOrLand,
                        GraveyardSearchScope.CONTROLLERS_GRAVEYARD)),
                0,
                2
        ).withActivationCondition(new Delirium(),
                "Activate only if there are four or more card types among cards in your graveyard.")
                .withMultiTargetConstraint(MultiTargetConstraint.AT_MOST_ONE_CREATURE_AND_ONE_LAND));
    }
}
