package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FRF", collectorNumber = "52")
public class ShuYunTheSilentTempest extends Card {

    public ShuYunTheSilentTempest() {
        CardNotPredicate noncreatureSpell = new CardNotPredicate(new CardTypePredicate(CardType.CREATURE));

        target(TargetFilters.creature()).addTrigger(Triggers.youCastSpell().withFilter(noncreatureSpell),
                new MayPayManaEffect("{R/W}{R/W}", new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, GrantScope.TARGET), "Pay {R/W}{R/W}?"));
    }
}
