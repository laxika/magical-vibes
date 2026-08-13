package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "51")
@CardRegistration(set = "TPR", collectorNumber = "78")
public class WaywardSoul extends Card {

    public WaywardSoul() {
        addActivatedAbility(new ActivatedAbility(false, "{U}", List.of(PutTargetOnTopOfLibraryEffect.self()),
                "{U}: Put this creature on top of its owner's library."));
    }
}
