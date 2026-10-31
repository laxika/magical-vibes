package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextMainPhaseEffect;

import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "65")
@CardRegistration(set = "NCC", collectorNumber = "165")
public class ViviensStampede extends Card {

    public ViviensStampede() {
        addSpellEffect(new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.MELEE), GrantScope.OWN_CREATURES));
        addSpellEffect(new RegisterDrawCardsAtNextMainPhaseEffect());
    }
}
