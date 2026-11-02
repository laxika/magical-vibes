package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExileNCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.KroxaDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessEscapedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "221")
@CardRegistration(set = "THB", collectorNumber = "340")
@CardRegistration(set = "SLD", collectorNumber = "225")
@CardRegistration(set = "MUL", collectorNumber = "49")
@CardRegistration(set = "MUL", collectorNumber = "114")
@CardRegistration(set = "MUL", collectorNumber = "179")
public class KroxaTitanOfDeathsHunger extends Card {

    public KroxaTitanOfDeathsHunger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT,
                new SacrificeSelfUnlessEscapedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), TriggerMode.INDEPENDENT, new KroxaDiscardEffect());
        addTrigger(Triggers.attacks(), new KroxaDiscardEffect());

        addCastingOption(new GraveyardCast(null, "{B}{B}{R}{R}", List.of(
                new ExileNCardsFromGraveyardCastingCost(null, "other cards", 5)),
                null, false, false, true));
    }
}
