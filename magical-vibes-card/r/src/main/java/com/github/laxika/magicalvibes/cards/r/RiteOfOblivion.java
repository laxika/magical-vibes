package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "237")
@CardRegistration(set = "DBL", collectorNumber = "237")
public class RiteOfOblivion extends Card {

    public RiteOfOblivion() {
        addCastingCost(new SacrificePermanentCost(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), "Sacrifice a nonland permanent"));
        target(TargetFilters.nonlandPermanent())
                .addSpellEffect(new ExileTargetPermanentEffect());
        addCastingOption(new FlashbackCast("{2}{W}{B}"));
    }
}
