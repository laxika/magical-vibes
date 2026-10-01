package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.SpellsCastFromOutsideHandThisTurn;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "WHO", collectorNumber = "87")
@CardRegistration(set = "WHO", collectorNumber = "386")
@CardRegistration(set = "WHO", collectorNumber = "692")
@CardRegistration(set = "WHO", collectorNumber = "977")
public class ImpendingFlux extends Card {

    public ImpendingFlux() {
        var damage = new Sum(new Fixed(1),
                new SpellsCastFromOutsideHandThisTurn(CountScope.CONTROLLER));
        addEffect(EffectSlot.SPELL, new DealDamageToPlayersEffect(damage, DamageRecipient.EACH_OPPONENT));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(damage, false, false,
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        addCastingOption(new ForetellCast("{1}{R}{R}"));
    }
}
