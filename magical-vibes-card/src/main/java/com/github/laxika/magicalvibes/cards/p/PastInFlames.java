package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToGraveyardCardsEffect;

import java.util.Set;

@CardRegistration(set = "ISD", collectorNumber = "155")
@CardRegistration(set = "MM3", collectorNumber = "105")
@CardRegistration(set = "SS3", collectorNumber = "4")
@CardRegistration(set = "TSR", collectorNumber = "350")
@CardRegistration(set = "SIS", collectorNumber = "43")
public class PastInFlames extends Card {

    public PastInFlames() {
        addEffect(EffectSlot.SPELL, new GrantFlashbackToGraveyardCardsEffect(
                Set.of(CardType.INSTANT, CardType.SORCERY)));
        addCastingOption(new FlashbackCast("{4}{R}"));
    }
}
