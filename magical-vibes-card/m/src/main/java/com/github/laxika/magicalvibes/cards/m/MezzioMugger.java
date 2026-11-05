package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachPlayersLibraryAndGrantPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAtEndStepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "49")
@CardRegistration(set = "NCC", collectorNumber = "149")
public class MezzioMugger extends Card {

    public MezzioMugger() {
        addCastingOption(AlternateHandCast.blitz("{2}{R}"));

        addTrigger(Triggers.attacks(),
                new ExileTopCardOfEachPlayersLibraryAndGrantPlayPermissionUntilEndOfTurnEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CastForAlternateCost()),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF),
                new SacrificeSelfAtEndStepEffect());
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new CastForAlternateCost()),
                new DrawCardEffect(1));
    }
}
