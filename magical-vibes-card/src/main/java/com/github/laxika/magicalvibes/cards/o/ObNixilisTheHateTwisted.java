package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "100")
public class ObNixilisTheHateTwisted extends Card {

    public ObNixilisTheHateTwisted() {
        // Whenever an opponent draws a card, Ob Nixilis deals 1 damage to that player.
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new DealDamageToPlayersEffect(1, DamageRecipient.TARGET_PLAYER));

        // −2: Destroy target creature. Its controller draws two cards.
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(new DestroyTargetPermanentThenEffect(
                        new DrawCardEffect(2), ThenEffectRecipient.TARGET_CONTROLLER)),
                "−2: Destroy target creature. Its controller draws two cards.",
                TargetFilters.creature()
        ));
    }
}
