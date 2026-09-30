package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachCurseToOpponentAndDrawEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnCurseAttachedToPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MIC", collectorNumber = "38")
@CardRegistration(set = "MIC", collectorNumber = "76")
public class LyndeCheerfulTormentor extends Card {

    public LyndeCheerfulTormentor() {
        addEffect(EffectSlot.ON_ALLY_PERMANENT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.CURSE),
                        new RegisterDelayedReturnCurseAttachedToPlayerEffect(null, null)));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new MayEffect(
                        new AttachCurseToOpponentAndDrawEffect(),
                        "Attach a Curse attached to you to an opponent and draw two cards?"));
    }
}
