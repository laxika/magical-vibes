package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "SLX", collectorNumber = "5")
public class HargildeKindlyRunechanter extends Card {

    public HargildeKindlyRunechanter() {
        // {T}: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.COLORLESS, 2, new ManaRestriction.ArtifactSpells())),
                "{T}: Add {C}{C}. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));
    }
}
