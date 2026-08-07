package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceIsRenowned;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;

import java.util.List;

@CardRegistration(set = "ORI", collectorNumber = "182")
public class HonoredHierarch extends Card {

    public HonoredHierarch() {
        // Renown 1
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new RenownEffect(1));

        // As long as Honored Hierarch is renowned, it has vigilance ...
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceIsRenowned(), new GrantKeywordEffect(Keyword.VIGILANCE, GrantScope.SELF)));

        // ... and "{T}: Add one mana of any color."
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new AwardAnyColorManaEffect()),
                "{T}: Add one mana of any color.")
                .withActivationCondition(new SourceIsRenowned(),
                        "Activate only while this creature is renowned"));
    }
}
