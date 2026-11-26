package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "100")
@CardRegistration(set = "SLD", collectorNumber = "511")
public class ObNixilisTheHateTwisted extends Card {

    public ObNixilisTheHateTwisted() {
        // Whenever an opponent draws a card, Ob Nixilis deals 1 damage to that player.
        addTrigger(Triggers.opponentDrawsCard(),
                DealDamageEffect.to(Subjects.triggeringPlayer(), 1));

        // −2: Destroy target creature. Its controller draws two cards.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(SequenceEffect.of(
                        new DestroyTargetPermanentEffect(),
                        new DrawCardEffect(2, Subjects.controllerOf(Subjects.target())))),
                "−2: Destroy target creature. Its controller draws two cards.",
                TargetFilters.creature()
        ));
    }
}
