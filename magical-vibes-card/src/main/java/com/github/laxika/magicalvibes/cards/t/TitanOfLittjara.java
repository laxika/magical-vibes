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

@CardRegistration(set = "CMM", collectorNumber = "728")
@CardRegistration(set = "CMM", collectorNumber = "760")
public class TitanOfLittjara extends Card {

    public TitanOfLittjara() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseSubtypeOnEnterEffect());
        addEffect(EffectSlot.STATIC, GrantChosenSubtypeToOwnCreaturesEffect.toSelf());

        PermanentCount otherMatchingCreatures = new PermanentCount(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentSharesCreatureTypeWithSourcePermanentPredicate())),
                CountScope.CONTROLLER,
                true);
        MayEffect drawAndDiscard = new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(otherMatchingCreatures),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw cards for each other creature sharing a creature type with Titan of Littjara?");
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, drawAndDiscard);
        addEffect(EffectSlot.ON_ATTACK, drawAndDiscard);
    }
}
