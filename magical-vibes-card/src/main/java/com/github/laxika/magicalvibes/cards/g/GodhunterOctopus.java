package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "JOU", collectorNumber = "39")
public class GodhunterOctopus extends Card {

    public GodhunterOctopus() {
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new AnyOf(List.of(
                        new DefendingPlayerControlsPermanent(new PermanentIsEnchantmentPredicate()),
                        new DefendingPlayerControlsPermanent(new PermanentIsEnchantedPredicate())
                )),
                "defending player controls an enchantment or an enchanted permanent"
        ));
    }
}
