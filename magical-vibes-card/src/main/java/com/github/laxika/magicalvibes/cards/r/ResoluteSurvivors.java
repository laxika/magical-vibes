package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;

@CardRegistration(set = "HOU", collectorNumber = "142")
public class ResoluteSurvivors extends Card {

    public ResoluteSurvivors() {
        // Exert: "You may exert this creature as it attacks." Modeled as an optional attack trigger
        // (matching Trueheart Twins); choosing to exert keeps the creature tapped through its next
        // untap step.
        //
        // "Whenever you exert a creature, this creature deals 1 damage to each opponent and you gain
        // 1 life." The engine has no exert-event slot, so the only exert it can observe is this
        // creature's own exert as it attacks — the damage/life payoff is bundled onto the exert when
        // it is accepted.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new SkipNextUntapEffect(TapUntapScope.SELF),
                        new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                        new GainLifeEffect(1)
                ),
                "Exert Resolute Survivors as it attacks? (It deals 1 damage to each opponent and you gain 1 life.)"
        ));
    }
}
