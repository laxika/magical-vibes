package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSelfFromGraveyardIntoLibraryEffect;

@CardRegistration(set = "PTK", collectorNumber = "6")
public class GuanYuSaintedWarrior extends Card {

    public GuanYuSaintedWarrior() {
        // Horsemanship is auto-loaded from Scryfall (evasion handled by the combat engine).
        // "When Guan Yu is put into your graveyard from the battlefield, you may shuffle Guan Yu
        // into your library."
        addEffect(EffectSlot.ON_DEATH, new MayEffect(
                new ShuffleSelfFromGraveyardIntoLibraryEffect(),
                "Shuffle Guan Yu into your library?"));
    }
}
