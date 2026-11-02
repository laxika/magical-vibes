package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileNonlandCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "566")
public class AmysHome extends Card {

    public AmysHome() {
        addTrigger(Triggers.planeswalkToThis(), new MayEffect(
                new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(),
                "Exile a nonland card from your hand?"));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new ExileNonlandCardFromHandWithManaValueTimeCountersEffect(),
                "Exile a nonland card from your hand?"));
        addTrigger(Triggers.chaosEnsues(), new TimeTravelEffect(1));
    }
}
