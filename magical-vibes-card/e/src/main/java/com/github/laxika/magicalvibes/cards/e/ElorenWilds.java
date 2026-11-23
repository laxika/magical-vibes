package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OHOP", collectorNumber = "7")
@CardRegistration(set = "OPCA", collectorNumber = "21")
public class ElorenWilds extends Card {

    public ElorenWilds() {
        addTrigger(Triggers.playerTapsPermanentForMana(),
                new AddManaOfTypeProducedByTappedPermanentEffect());
        addTrigger(Triggers.chaosEnsues(),
                new PlayerRestrictionEffect(Subjects.targetPlayer(),
                        PlayerRestriction.castingSpells(), Duration.UNTIL_PLANESWALK));
    }
}
