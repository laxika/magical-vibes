package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.IslandSanctuaryEffect;

@CardRegistration(set = "5ED", collectorNumber = "39")
public class IslandSanctuary extends Card {

    public IslandSanctuary() {
        // If you would draw a card during your draw step, instead you may skip that draw. If you do,
        // until your next turn, you can't be attacked except by creatures with flying and/or islandwalk.
        // The draw-step skip prompt and the resulting attack-restriction shield are driven by the
        // MAY_SKIP_DRAW_STEP_DRAW slot in StepTriggerService.handleDrawStep.
        addEffect(EffectSlot.MAY_SKIP_DRAW_STEP_DRAW, new IslandSanctuaryEffect());
    }
}
