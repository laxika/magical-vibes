package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TLA", collectorNumber = "245")
public class TollsOfWar extends Card {

    public TollsOfWar() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofClueToken(1));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_SACRIFICED,
                new OncePerTurnTriggerEffect(new ConditionalEffect(
                        new ControllerTurn(),
                        new CreateTokenEffect("Ally", 1, 1, CardColor.WHITE,
                                List.of(CardSubtype.ALLY), Set.of(), Set.of()))));
    }
}
