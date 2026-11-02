package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "99")
public class SchemingSilvertongueSignInBlood extends Card {

    public SchemingSilvertongueSignInBlood() {
        setBackFaceCard(new SignInBlood());

        addTrigger(Triggers.postcombatMainPhase(ActivePlayer.YOU),
                new ConditionalEffect(new GainedLifeThisTurn(2), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SignInBlood";
    }
}
