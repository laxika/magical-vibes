package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "116")
public class Bloodcurdler extends Card {

    public Bloodcurdler() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MillEffect(1, Subjects.you()));

        var threshold = new GraveyardCardThreshold(7, null);
        addStaticEffect(new ConditionalEffect(threshold,
                new StaticBoostEffect(1, 1, Subjects.source())));
        addStaticEffect(new ConditionalEffect(threshold,
                new GrantTriggeredAbilityEffect(
                        new TriggeredAbility(Triggers.endStep(ActivePlayer.YOU),
                        new ExileGraveyardCardsEffect(2, GraveyardExileScope.OWN)),
                        Subjects.source())));
    }
}
