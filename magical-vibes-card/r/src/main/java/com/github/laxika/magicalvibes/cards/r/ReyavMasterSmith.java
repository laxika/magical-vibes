package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEquippedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "MUL", collectorNumber = "57")
@CardRegistration(set = "MUL", collectorNumber = "122")
@CardRegistration(set = "MUL", collectorNumber = "187")
public class ReyavMasterSmith extends Card {

    public ReyavMasterSmith() {
        PermanentPredicate enchantedOrEquipped = new PermanentAnyOfPredicate(List.of(
                new PermanentIsEnchantedPredicate(),
                new PermanentIsEquippedPredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(enchantedOrEquipped,
                        new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TRIGGERING_PERMANENT)));
    }
}
