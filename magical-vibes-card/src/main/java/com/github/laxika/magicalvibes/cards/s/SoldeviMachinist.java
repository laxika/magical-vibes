package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "102")
public class SoldeviMachinist extends Card {

    public SoldeviMachinist() {
        // {T}: Add {C}{C}. Spend this mana only to activate abilities of artifacts.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(ManaColor.COLORLESS, 2, new ManaRestriction.ArtifactAbilities())),
                "{T}: Add {C}{C}. Spend this mana only to activate abilities of artifacts."
        ));
    }
}
