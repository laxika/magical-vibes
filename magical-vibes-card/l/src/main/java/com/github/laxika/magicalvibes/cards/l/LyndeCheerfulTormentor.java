package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AttachCurseToOpponentAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIC", collectorNumber = "38")
@CardRegistration(set = "MIC", collectorNumber = "76")
public class LyndeCheerfulTormentor extends Card {

    public LyndeCheerfulTormentor() {
        addTrigger(Triggers.dies(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.NONTOKEN,
                                new PermanentHasSubtypePredicate(CardSubtype.CURSE))),
                        ObjectRelation.YOU_OWN),
                new RegisterDelayedReturnCurseAttachedToPlayerEffect(null, null));

        addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                new MayEffect(
                        new AttachCurseToOpponentAndDrawEffect(),
                        "Attach a Curse attached to you to an opponent and draw two cards?"));
    }
}
