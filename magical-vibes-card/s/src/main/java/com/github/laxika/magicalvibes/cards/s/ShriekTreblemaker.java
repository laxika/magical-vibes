package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "144")
@CardRegistration(set = "OM1", collectorNumber = "131")
public class ShriekTreblemaker extends Card {

    public ShriekTreblemaker() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new MayEffect(
                new DiscardCardThenEffect(
                        null,
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                        "a card"),
                "Discard a card to have target creature unable to block this turn?"));

        addTrigger(Triggers.creatureDies(ObjectRelation.OPPONENT_CONTROLS),
                new DealDamageToPlayersEffect(1, DamageRecipient.TRIGGERING_PLAYER));
    }
}
