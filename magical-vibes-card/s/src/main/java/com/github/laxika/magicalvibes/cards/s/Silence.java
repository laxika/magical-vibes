package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.PlayerRestriction;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "M10", collectorNumber = "31")
@CardRegistration(set = "M11", collectorNumber = "30")
@CardRegistration(set = "M14", collectorNumber = "35")
@CardRegistration(set = "SLD", collectorNumber = "881")
@CardRegistration(set = "SLD", collectorNumber = "1445")
@CardRegistration(set = "SLD", collectorNumber = "1816")
@CardRegistration(set = "SLD", collectorNumber = "2300")
@CardRegistration(set = "TSR", collectorNumber = "302")
@CardRegistration(set = "PIO", collectorNumber = "32")
public class Silence extends Card {

    public Silence() {
        addSpellEffect(PlayerRestrictionEffect.thisTurn(Subjects.eachOpponent(),
                PlayerRestriction.castingSpells()));
    }
}
