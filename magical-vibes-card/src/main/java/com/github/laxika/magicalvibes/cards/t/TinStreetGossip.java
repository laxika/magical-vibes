package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "235")
public class TinStreetGossip extends Card {

    public TinStreetGossip() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new AwardRestrictedManaEffect(ManaColor.RED, 1,
                                new ManaRestriction.FaceDownSpellsOrTurnFaceUp()),
                        new AwardRestrictedManaEffect(ManaColor.GREEN, 1,
                                new ManaRestriction.FaceDownSpellsOrTurnFaceUp())
                ),
                "{T}: Add {R}{G}. Spend this mana only to cast face-down spells or turn creatures face up."
        ));
    }
}
