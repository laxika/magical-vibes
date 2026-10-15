package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainActivatedAbilitiesOfTopLibraryCardEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithTopCardRevealedEffect;

@CardRegistration(set = "ALA", collectorNumber = "56")
public class SkillBorrower extends Card {

    public SkillBorrower() {
        // Play with the top card of your library revealed.
        addEffect(EffectSlot.STATIC, new PlayWithTopCardRevealedEffect());
        // As long as the top card of your library is an artifact or creature card,
        // this creature has all activated abilities of that card.
        addEffect(EffectSlot.STATIC, new GainActivatedAbilitiesOfTopLibraryCardEffect());
    }
}
