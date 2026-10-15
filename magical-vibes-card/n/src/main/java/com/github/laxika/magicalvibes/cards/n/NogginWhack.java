package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.TargetRevealsCardsControllerChoosesDiscardEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "70")
public class NogginWhack extends Card {

    public NogginWhack() {
        // Prowl {1}{B}: cast for this cost if you dealt combat damage to a player this turn with a Rogue.
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}")), CardSubtype.ROGUE));

        // Target player reveals three cards from their hand. You choose two of them. That player
        // discards those cards. (canTargetPlayer() supplies the target-player requirement.)
        addEffect(EffectSlot.SPELL, new TargetRevealsCardsControllerChoosesDiscardEffect(3, 2));
    }
}
