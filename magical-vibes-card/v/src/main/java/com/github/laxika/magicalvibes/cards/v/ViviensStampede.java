package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextMainPhaseEffect;

import java.util.Set;

@CardRegistration(set = "NCC", collectorNumber = "65")
@CardRegistration(set = "NCC", collectorNumber = "165")
public class ViviensStampede extends Card {

    public ViviensStampede() {
        addEffect(EffectSlot.SPELL, new GrantKeywordEffect(
                Set.of(Keyword.VIGILANCE, Keyword.TRAMPLE, Keyword.MELEE), GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.SPELL, new RegisterDrawCardsAtNextMainPhaseEffect());
    }
}
