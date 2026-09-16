package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellOnSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "2XM", collectorNumber = "214")
public class RikuOfTwoReflections extends Card {

    public RikuOfTwoReflections() {
        // Whenever you cast an instant or sorcery spell, you may pay {U}{R}. If you do, copy that
        // spell. You may choose new targets for the copy.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new CopyControllerCastSpellOnSpellCastEffect(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT),
                        new CardTypePredicate(CardType.SORCERY)
                )),
                "{U}{R}"
        ));

        // Whenever another nontoken creature you control enters, you may pay {G}{U}. If you do,
        // create a token that's a copy of that creature.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, new MayPayManaEffect(
                "{G}{U}",
                new CreateTokenCopyOfTargetPermanentEffect(),
                "Pay {G}{U} to create a token that's a copy of that creature?"
        ));
    }
}
