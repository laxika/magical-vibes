package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "151")
public class ScourgeOfValkas extends Card {

    public ScourgeOfValkas() {
        // Flying is auto-loaded as a keyword from Scryfall.

        // Whenever this creature or another Dragon you control enters, it deals X damage to any
        // target, where X is the number of Dragons you control. The count resolves on resolution,
        // so the entering Dragon (already on the battlefield) counts itself.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, dragonDamage());
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DRAGON), dragonDamage()));

        // {R}: This creature gets +1/+0 until end of turn.
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(new BoostSelfEffect(1, 0)),
                "{R}: Scourge of Valkas gets +1/+0 until end of turn."));
    }

    private static DealDamageToAnyTargetEffect dragonDamage() {
        return new DealDamageToAnyTargetEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.DRAGON), CountScope.CONTROLLER));
    }
}
