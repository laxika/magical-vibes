package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToOnePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnChosenPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostPredicate;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "97")
public class GixsCommand extends Card {

    public GixsCommand() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put two +1/+1 counters on up to one creature. It gains lifelink until end of turn",
                        SequenceEffect.of(
                                new ChooseUpToOnePermanentEffect(new PermanentIsCreaturePredicate(),
                                        "Choose up to one creature to get two +1/+1 counters and lifelink."),
                                new PutCountersOnChosenPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new GrantKeywordToChosenCreatureEffect(Keyword.LIFELINK, GrantDuration.END_OF_TURN))),
                new ChooseOneEffect.ChooseOneOption(
                        "Destroy each creature with power 2 or less",
                        new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtMostPredicate(2))))),
                new ChooseOneEffect.ChooseOneOption(
                        "Return up to two creature cards from your graveyard to your hand",
                        new ReturnCardsFromControllerGraveyardToHandEffect(
                                new CardTypePredicate(CardType.CREATURE), new Fixed(2))),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent sacrifices a creature with the greatest power among creatures they control",
                        new SacrificePermanentsEffect(1,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                                SacrificeRecipient.EACH_OPPONENT))
        ), 2));
    }
}
