package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceSecondSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;

@CardRegistration(set = "FIC", collectorNumber = "9")
@CardRegistration(set = "FIC", collectorNumber = "129")
public class AlisaieLeveilleur extends Card {

    private static final String PARTNER_NAME = "Alphinaud Leveilleur";

    public AlisaieLeveilleur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Alphinaud Leveilleur into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addEffect(EffectSlot.STATIC, new ReduceSecondSpellCastCostEffect(2));
    }
}
