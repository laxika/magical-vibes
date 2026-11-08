package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CommanderCastsFromCommandZoneThisGame;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "221")
@CardRegistration(set = "C18", collectorNumber = "7")
public class EchoStorm extends Card {

    public EchoStorm() {
        addTrigger(Triggers.youCastThisSpell(),
                new CopyTriggeringSpellEffect().withCopies(new CommanderCastsFromCommandZoneThisGame()));
        target(TargetFilters.artifact())
                .addSpellEffect(new CreateTokenCopyOfTargetPermanentEffect());
    }
}
