package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "LCI", collectorNumber = "169")
public class TectonicHazard extends Card {

    public TectonicHazard() {
        addSimultaneousSpellEffects(new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT),
                new MassDamageEffect(1, false, false,
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
