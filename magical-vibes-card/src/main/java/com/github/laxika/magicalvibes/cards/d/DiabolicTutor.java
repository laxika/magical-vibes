package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "135")
@CardRegistration(set = "M10", collectorNumber = "91")
@CardRegistration(set = "M11", collectorNumber = "93")
@CardRegistration(set = "M12", collectorNumber = "92")
@CardRegistration(set = "M14", collectorNumber = "95")
@CardRegistration(set = "9ED", collectorNumber = "125")
@CardRegistration(set = "8ED", collectorNumber = "128")
@CardRegistration(set = "ODY", collectorNumber = "129")
@CardRegistration(set = "KLD", collectorNumber = "75")
public class DiabolicTutor extends Card {

    public DiabolicTutor() {
        addEffect(EffectSlot.SPELL, new SearchLibraryEffect());
    }
}
