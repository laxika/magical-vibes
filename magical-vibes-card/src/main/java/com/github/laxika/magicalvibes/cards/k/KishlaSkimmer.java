package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "TDM", collectorNumber = "201")
public class KishlaSkimmer extends Card {

    public KishlaSkimmer() {
        addEffect(EffectSlot.ON_CONTROLLER_CARDS_LEAVE_GRAVEYARD,
                new OncePerTurnTriggerEffect(new ConditionalEffect(
                        new ControllerTurn(), new DrawCardEffect(1))));
    }
}
