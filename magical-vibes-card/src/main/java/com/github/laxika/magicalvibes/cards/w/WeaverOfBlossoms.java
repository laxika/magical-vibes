package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.b.BlossomCladWerewolf;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

import java.util.List;

@CardRegistration(set = "VOW", collectorNumber = "226")
public class WeaverOfBlossoms extends Card {

    public WeaverOfBlossoms() {
        setBackFaceCard(new BlossomCladWerewolf());

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color."
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "BlossomCladWerewolf";
    }
}
