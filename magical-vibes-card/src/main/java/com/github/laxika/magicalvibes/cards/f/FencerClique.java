package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "33")
public class FencerClique extends Card {

    public FencerClique() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(PutTargetOnTopOfLibraryEffect.self()),
                "{U}: Put this creature on top of its owner's library."));
    }
}
