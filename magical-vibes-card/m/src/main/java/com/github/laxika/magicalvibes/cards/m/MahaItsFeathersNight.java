package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "100")
public class MahaItsFeathersNight extends Card {

    public MahaItsFeathersNight() {
        // Ward—Discard a card. (Whenever this creature becomes the target of a spell or ability
        // an opponent controls, counter it unless that player discards a card.)
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(), new CounterUnlessDiscardsEffect());

        addStaticEffect(new SetBasePowerToughnessEffect(null, 1, GrantScope.OPPONENT_CREATURES));
    }
}
