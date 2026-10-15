package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "105")
public class LetsPlayAGame extends Card {

    public LetsPlayAGame() {
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMoreWhen(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Creatures your opponents control get -1/-1 until end of turn",
                        new BoostAllCreaturesEffect(-1, -1,
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()))),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent discards two cards",
                        new DiscardEffect(2, DiscardRecipient.EACH_OPPONENT)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each opponent loses 3 life and you gain 3 life",
                        List.of(
                                new LoseLifeEffect(3, LoseLifeRecipient.EACH_OPPONENT),
                                new GainLifeEffect(3)))
        ), new Delirium()));
    }
}
