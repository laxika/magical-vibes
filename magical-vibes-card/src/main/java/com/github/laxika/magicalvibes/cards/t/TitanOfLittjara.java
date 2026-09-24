package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantChosenSubtypeToOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentSharesCreatureTypeWithSourcePermanentPredicate;

import java.util.List;

@CardRegistration(set = "MSC", collectorNumber = "280")
@CardRegistration(set = "MSC", collectorNumber = "335")
@CardRegistration(set = "CMM", collectorNumber = "728")
@CardRegistration(set = "CMM", collectorNumber = "760")
public class TitanOfLittjara extends Card {

    public TitanOfLittjara() {
        // As this creature enters, choose a creature type.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());

        // This creature is the chosen type in addition to its other types.
        addEffect(EffectSlot.STATIC, GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        // Whenever this creature enters or attacks, you may draw a card for each other creature
        // you control that shares a creature type with it. If you do, discard a card.
        var drawAndDiscard = new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(new PermanentCount(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentSharesCreatureTypeWithSourcePermanentPredicate())),
                                CountScope.CONTROLLER,
                                true)),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw cards and discard a card?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, drawAndDiscard);
        addEffect(EffectSlot.ON_ATTACK, drawAndDiscard);
    }
}
