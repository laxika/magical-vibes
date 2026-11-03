package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchTargetPlayerLibraryForNamedCardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "33")
@CardRegistration(set = "FIC", collectorNumber = "140")
public class AlphinaudLeveilleur extends Card {

    private static final String PARTNER_NAME = "Alisaie Leveilleur";

    public AlphinaudLeveilleur() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new SearchTargetPlayerLibraryForNamedCardToHandEffect(PARTNER_NAME),
                "Have target player put Alisaie Leveilleur into their hand from their library?",
                null,
                MayChoicePlayer.TARGET_PLAYER));
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new DrawCardEffect());
    }
}
