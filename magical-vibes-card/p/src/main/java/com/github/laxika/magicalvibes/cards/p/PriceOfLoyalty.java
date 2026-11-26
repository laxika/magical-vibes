package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TreasureManaSpentToCast;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AFR", collectorNumber = "159")
public class PriceOfLoyalty extends Card {

    public PriceOfLoyalty() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, Subjects.target()))
                .addSpellEffect(new ConditionalEffect(
                        new TreasureManaSpentToCast(),
                        BoostEffect.target(2, 0)));
    }
}
