package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.AwardRestrictedManaEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ManaRestriction;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "FDN", collectorNumber = "141")
@CardRegistration(set = "SNC", collectorNumber = "14")
public class GiadaFontOfHope extends Card {

    public GiadaFontOfHope() {
        addEffect(EffectSlot.STATIC, new ControlledCreaturesEnterWithAdditionalCountersEffect(
                CardSubtype.ANGEL,
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ANGEL), CountScope.CONTROLLER)
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardRestrictedManaEffect(
                        ManaColor.WHITE,
                        1,
                        new ManaRestriction.SubtypeCreatureSpells(CardSubtype.ANGEL)
                )),
                "{T}: Add {W}. Spend this mana only to cast an Angel spell."
        ));
    }
}
