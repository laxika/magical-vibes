package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StateTriggerEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "68")
public class LastLaugh extends Card {

    public LastLaugh() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new MassDamageEffect(1, true));

        addEffect(EffectSlot.STATE_TRIGGERED, new StateTriggerEffect(
                (gameData, sourcePermanent, controllerId) -> gameData.playerBattlefields.values().stream()
                        .flatMap(List::stream)
                        .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE)),
                List.of(new SacrificeSelfEffect()),
                "Last Laugh's state-triggered ability"
        ));
    }
}
