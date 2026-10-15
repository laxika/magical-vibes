package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "149")
public class SlobadIronGoblin extends Card {

    public SlobadIronGoblin() {
        // {T}, Sacrifice an artifact: Add an amount of {R} equal to the sacrificed artifact's mana
        // value. Spend this mana only to cast artifact spells or activate abilities of artifacts.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsArtifactPredicate(), "Sacrifice an artifact", false, false, true, false),
                        new AwardRestrictedManaEffect(ManaColor.RED, new XValue(), new ManaRestriction.ArtifactSpells())),
                "{T}, Sacrifice an artifact: Add an amount of {R} equal to the sacrificed artifact's mana value. Spend this mana only to cast artifact spells or activate abilities of artifacts."
        ));
    }
}
