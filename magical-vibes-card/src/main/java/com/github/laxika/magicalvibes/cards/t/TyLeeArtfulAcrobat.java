package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "158")
public class TyLeeArtfulAcrobat extends Card {

    public TyLeeArtfulAcrobat() {
        // Prowess is loaded from Scryfall as a keyword, but its trigger is represented here.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                List.of(new BoostSelfEffect(1, 1))
        ));

        // Whenever Ty Lee attacks, you may pay {1}. When you do, target creature can't block this turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ATTACK,
                new MayPayManaEffect("{1}",
                        new CantBlockThisTurnEffect(TapUntapScope.TARGET),
                        "Pay {1} to make target creature unable to block this turn?"));
    }
}
