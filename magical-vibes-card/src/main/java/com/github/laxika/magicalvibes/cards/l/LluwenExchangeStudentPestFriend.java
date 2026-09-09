package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.p.PestFriend;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;

import java.util.List;

/** Lluwen, Exchange Student // Pest Friend (SOS 199). */
@CardRegistration(set = "SOS", collectorNumber = "199")
public class LluwenExchangeStudentPestFriend extends Card {

    public LluwenExchangeStudentPestFriend() {
        setBackFaceCard(new PestFriend());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), new BecomePreparedEffect()),
                "Exile a creature card from your graveyard: Lluwen becomes prepared. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "PestFriend";
    }
}
