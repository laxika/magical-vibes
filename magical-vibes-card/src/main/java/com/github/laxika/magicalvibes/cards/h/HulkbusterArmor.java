package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "107")
@CardRegistration(set = "MSC", collectorNumber = "438")
public class HulkbusterArmor extends Card {

    public HulkbusterArmor() {
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(9, 9, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));

        var hero = new PermanentHasSubtypePredicate(CardSubtype.HERO);
        addActivatedAbility(new EquipActivatedAbility(
                "{3}", hero, "Target must be a Hero creature you control"));
        addActivatedAbility(new EquipActivatedAbility("{6}"));
    }
}
