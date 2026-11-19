package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "162")
public class VengefulPossession extends Card {

    public VengefulPossession() {
        target(TargetFilters.creature())
                .addSpellEffect(new GainControlOfTargetEffect(Duration.UNTIL_END_OF_TURN))
                .addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new GrantKeywordEffect(Keyword.HASTE, GrantScope.TARGET))
                .addSpellEffect(new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
