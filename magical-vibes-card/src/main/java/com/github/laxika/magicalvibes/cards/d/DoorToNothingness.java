package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;

import java.util.List;

@CardRegistration(set = "M13", collectorNumber = "203")
public class DoorToNothingness extends Card {

    public DoorToNothingness() {
        // This artifact enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // {W}{W}{U}{U}{B}{B}{R}{R}{G}{G}, {T}, Sacrifice this artifact: Target player loses the game.
        // playerId is null so the handler resolves the player chosen for the stack entry.
        addActivatedAbility(new ActivatedAbility(true, "{W}{W}{U}{U}{B}{B}{R}{R}{G}{G}",
                List.of(new SacrificeSelfCost(), new TargetPlayerLosesGameEffect(null)),
                "{W}{W}{U}{U}{B}{B}{R}{R}{G}{G}, {T}, Sacrifice this artifact: Target player loses the game."));
    }
}
