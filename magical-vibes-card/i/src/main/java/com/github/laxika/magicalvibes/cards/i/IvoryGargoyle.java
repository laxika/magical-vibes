package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALL", collectorNumber = "5")
@CardRegistration(set = "ME2", collectorNumber = "19")
public class IvoryGargoyle extends Card {

    public IvoryGargoyle() {
        // Flying is auto-loaded from Scryfall keywords.
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                RegisterDelayedStepTriggerEffect.aboutCardAtNext(
                        Triggers.endStep(ActivePlayer.ANY), Subjects.sourceCard(),
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect()),
                new SkipNextEffect(SkipKind.DRAW_STEP)));

        addActivatedAbility(new ActivatedAbility(false, "{4}{W}",
                List.of(new ExileTargetPermanentEffect(Subjects.source())),
                "{4}{W}: Exile this creature."));
    }
}
