package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.ReverseTurnOrderEffect;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "52")
public class AeonEngine extends Card {

    public AeonEngine() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new ExileSelfCost(), new ReverseTurnOrderEffect()),
                "{T}, Exile this artifact: Reverse the game's turn order."
        ));
    }
}
