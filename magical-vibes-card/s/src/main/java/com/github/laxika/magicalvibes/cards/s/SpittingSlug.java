package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentInCombatWithSourcePredicate;

@CardRegistration(set = "DRK", collectorNumber = "88")
@CardRegistration(set = "TSB", collectorNumber = "85")
public class SpittingSlug extends Card {

    public SpittingSlug() {
        GrantKeywordEffect firstStrikeToSlug = new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF);
        GrantKeywordEffect firstStrikeToCombatOpponents = new GrantKeywordEffect(
                Keyword.FIRST_STRIKE,
                GrantScope.ALL_CREATURES,
                new PermanentInCombatWithSourcePredicate());
        MayPayManaEffect mayPay = new MayPayManaEffect(
                "{1}{G}",
                firstStrikeToSlug,
                "Pay {1}{G} to give Spitting Slug first strike?",
                firstStrikeToCombatOpponents);

        addEffect(EffectSlot.ON_BLOCK, mayPay, TriggerMode.ONCE_PER_BLOCK);
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, mayPay, TriggerMode.ONCE_PER_BLOCK);
    }
}
