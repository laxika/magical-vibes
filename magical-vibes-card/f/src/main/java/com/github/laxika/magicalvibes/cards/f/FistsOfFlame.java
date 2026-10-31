package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisTurn;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MH1", collectorNumber = "123")
@CardRegistration(set = "TLE", collectorNumber = "166")
@CardRegistration(set = "CMM", collectorNumber = "223")
public class FistsOfFlame extends Card {

    public FistsOfFlame() {
        target(TargetFilters.creature())
                .addSpellEffect(new DrawCardEffect())
                .addSpellEffect(new BoostTargetCreatureEffect(
                        new CardsDrawnThisTurn(), new Fixed(0)))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET));
    }
}
