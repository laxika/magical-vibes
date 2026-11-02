package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DidntActivateLoyaltyAbilityThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantExtraLoyaltyActivationToPlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M15", collectorNumber = "215")
@CardRegistration(set = "PIO", collectorNumber = "358")
public class TheChainVeil extends Card {

    public TheChainVeil() {
        // At the beginning of your end step, if you didn't activate a loyalty ability of a
        // planeswalker this turn, you lose 2 life.
        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new DidntActivateLoyaltyAbilityThisTurn(),
                new LoseLifeEffect(2)));

        // {4}, {T}: For each planeswalker you control, you may activate one of its loyalty abilities
        // once this turn as though none of its loyalty abilities have been activated this turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new GrantExtraLoyaltyActivationToPlaneswalkersEffect()),
                "{4}, {T}: For each planeswalker you control, you may activate one of its loyalty "
                        + "abilities once this turn as though none of its loyalty abilities have been "
                        + "activated this turn."
        ));
    }
}
