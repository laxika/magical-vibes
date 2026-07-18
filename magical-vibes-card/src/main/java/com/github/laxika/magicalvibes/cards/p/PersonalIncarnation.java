package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToSelfToOwnerEffect;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "51")
@CardRegistration(set = "4ED", collectorNumber = "40")
public class PersonalIncarnation extends Card {

    public PersonalIncarnation() {
        // {0}: The next 1 damage that would be dealt to this creature this turn is dealt to its owner
        // instead. Only this creature's owner may activate this ability — abilities can only be activated
        // by the controller, who is the owner in this engine, so no extra restriction is needed.
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new RedirectNextDamageToSelfToOwnerEffect(1)),
                "{0}: The next 1 damage that would be dealt to this creature this turn is dealt to its owner instead. Only this creature's owner may activate this ability."));

        // When this creature dies, its owner loses half their life, rounded up.
        addEffect(EffectSlot.ON_DEATH,
                new LoseLifeEffect(new HalfControllerLifeRoundedUp(), LoseLifeRecipient.CONTROLLER));
    }
}
