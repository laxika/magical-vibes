package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;

@CardRegistration(set = "DMC", collectorNumber = "28")
@CardRegistration(set = "DMC", collectorNumber = "80")
public class TheManaRig extends Card {

    public TheManaRig() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardIsMulticoloredPredicate(),
                List.of(CreateTokenEffect.ofPowerstoneToken(new Fixed(1)))));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{X}{X}{X}",
                List.of(LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(new XValue(), 2)),
                "{X}{X}{X}, {T}: Look at the top X cards of your library. Put up to two of them into your hand and the rest on the bottom of your library in a random order."
        ));
    }
}
