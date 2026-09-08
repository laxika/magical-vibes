package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.HalfControllerLifeRoundedUp;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectRole;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "51")
@CardRegistration(set = "4ED", collectorNumber = "40")
@CardRegistration(set = "SUM", collectorNumber = "31")
public class PersonalIncarnation extends Card {

    public PersonalIncarnation() {
        addActivatedAbility(new ActivatedAbility(false, "{0}",
                List.of(new RedirectNextDamageEffect(RedirectRole.SOURCE_PERMANENT, RedirectRole.CONTROLLER,
                        1, null)),
                "{0}: The next 1 damage that would be dealt to this creature this turn is dealt to its owner instead. Only this creature's owner may activate this ability.")
                .withActivatableOnlyByOwner());

        addEffect(EffectSlot.ON_DEATH,
                new LoseLifeEffect(new HalfControllerLifeRoundedUp(), LoseLifeRecipient.OWNER));
    }
}
