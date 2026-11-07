package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DSC", collectorNumber = "19")
@CardRegistration(set = "DSC", collectorNumber = "49")
public class DemonicCovenant extends Card {

    public DemonicCovenant() {
        addTrigger(Triggers.youAttackPlayer(),
                TriggerOptions.DEFAULT.withEventCondition(
                        new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.DEMON))),
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                SequenceEffect.of(
                        new CreateTokenEffect("Demon", 5, 5, CardColor.BLACK,
                                List.of(CardSubtype.DEMON), Set.of(Keyword.FLYING), Set.of()),
                        new MillTwoThenSacrificeSelfIfMilledCardsShareAllTypesEffect()));
    }
}
