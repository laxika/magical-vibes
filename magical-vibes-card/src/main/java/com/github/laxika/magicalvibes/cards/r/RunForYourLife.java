package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureBlockableOnlyByFilterThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "154")
public class RunForYourLife extends Card {

    public RunForYourLife() {
        // One or two target creatures gain haste and can be blocked only by creatures with haste this turn.
        target(TargetFilters.creature(), 1, 2)
                .addEffect(EffectSlot.SPELL, new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addEffect(EffectSlot.SPELL, new MakeCreatureBlockableOnlyByFilterThisTurnEffect(
                        new PermanentHasKeywordPredicate(Keyword.HASTE), "creatures with haste"));

        addCastingOption(new GraveyardCast(null, "{2}{U}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 4)),
                null, false, false, true));
    }
}
