package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveSourceFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "95")
public class ShakedownHeavy extends Card {

    public ShakedownHeavy() {
        addTrigger(Triggers.attacks(), new MayEffect(
                SequenceEffect.of(
                        new DrawCardEffect(),
                        new UntapPermanentsEffect(TapUntapScope.SELF),
                        new RemoveSourceFromCombatEffect()),
                "Have this creature's controller draw a card?",
                null,
                MayChoicePlayer.DEFENDING_PLAYER));
    }
}
