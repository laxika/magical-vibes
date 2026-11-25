package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;

@CardRegistration(set = "PCY", collectorNumber = "77")
public class RhysticTutor extends Card {

    public RhysticTutor() {
        addSpellEffect(UnlessPaysEffect.unlessPays(Subjects.eachPlayer(), "{2}", new SearchLibraryEffect()));
    }
}
