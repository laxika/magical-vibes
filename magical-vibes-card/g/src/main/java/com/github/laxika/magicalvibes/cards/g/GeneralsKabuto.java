package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "CHK", collectorNumber = "251")
public class GeneralsKabuto extends Card {

    public GeneralsKabuto() {
        // Equipped creature has shroud.
        addStaticEffect(new GrantKeywordEffect(Keyword.SHROUD, Subjects.attachedCreature()));

        // Prevent all combat damage that would be dealt to equipped creature.
        addStaticEffect(new PreventAllCombatDamageToSelfEffect(Subjects.attachedCreature()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
