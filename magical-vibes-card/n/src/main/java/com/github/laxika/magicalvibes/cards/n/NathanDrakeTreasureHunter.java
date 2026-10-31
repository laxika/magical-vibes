package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.SpendAnyManaTypeToCastSpellsYouDontOwnEffect;
import com.github.laxika.magicalvibes.model.effect.SpendManaAsAnyColorForActivatedAbilitiesOfNonOwnedPermanentsEffect;

@CardRegistration(set = "SLD", collectorNumber = "2216")
public class NathanDrakeTreasureHunter extends Card {

    public NathanDrakeTreasureHunter() {
        addStaticEffect(new SpendAnyManaTypeToCastSpellsYouDontOwnEffect());
        addStaticEffect(new SpendManaAsAnyColorForActivatedAbilitiesOfNonOwnedPermanentsEffect());
        addEffect(EffectSlot.ON_ATTACK, new ExileTopCardOfEachPlayersLibraryAndMayCastSpellsEffect());
    }
}
