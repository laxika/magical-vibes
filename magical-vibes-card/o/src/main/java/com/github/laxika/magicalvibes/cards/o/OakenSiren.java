package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "66")
public class OakenSiren extends Card {

    public OakenSiren() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.BLUE, 1, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {U}. Spend this mana only to cast an artifact spell or activate an ability of an artifact source."
        ));
    }
}
