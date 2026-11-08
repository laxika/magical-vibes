package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellOrAbilityEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "C21", collectorNumber = "48")
public class BattlemagesBracers extends Card {

    public BattlemagesBracers() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.EQUIPPED_CREATURE));

        // Whenever an ability of equipped creature is activated, if it isn't a mana ability, you may pay {1}.
        // If you do, copy that ability. You may choose new targets for the copy.
        addTrigger(Triggers.playerActivatesNonManaAbility().withSource(new PermanentIsHostOfSourceAuraPredicate()),
                new MayPayManaEffect("{1}", new CopyTriggeringSpellOrAbilityEffect(),
                        "Pay {1} to copy that ability?"));

        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
