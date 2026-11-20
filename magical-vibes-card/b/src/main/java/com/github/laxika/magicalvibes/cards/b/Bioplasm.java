package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "GPT", collectorNumber = "81")
public class Bioplasm extends Card {

    public Bioplasm() {
        addTrigger(Triggers.attacks(), SequenceEffect.of(
                new ExileTopCardOfOwnLibraryEffect(false),
                new ConditionalEffect(HappenedThisWay.toACard(StepKind.EXILE, new CardTypePredicate(CardType.CREATURE)),
                        new BoostSelfEffect(AmountThisWay.exiledPower(), AmountThisWay.exiledToughness()))));
    }
}
