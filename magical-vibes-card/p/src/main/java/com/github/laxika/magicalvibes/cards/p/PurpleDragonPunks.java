package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "TMT", collectorNumber = "100")
public class PurpleDragonPunks extends Card {

    public PurpleDragonPunks() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.RED, 1, new ManaRestriction.ArtifactSpellsOrAbilities())),
                "{T}: Add {R}. Spend this mana only to cast an artifact spell or to activate an ability."
        ));
    }
}
