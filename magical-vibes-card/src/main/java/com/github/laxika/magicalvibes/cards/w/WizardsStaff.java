package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;

@CardRegistration(set = "HOB", collectorNumber = "59")
public class WizardsStaff extends Card {

    public WizardsStaff() {
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.PROWESS, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new AdditionalTriggeredAbilityEffect(new PermanentIsEquippedPredicate()));
        addActivatedAbility(new EquipActivatedAbility(
                "{1}", new PermanentHasSubtypePredicate(CardSubtype.WIZARD),
                "Target must be a Wizard creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
