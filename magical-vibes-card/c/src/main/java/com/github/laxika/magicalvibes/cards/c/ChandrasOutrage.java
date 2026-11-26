package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "M11", collectorNumber = "128")
@CardRegistration(set = "M12", collectorNumber = "125")
@CardRegistration(set = "M14", collectorNumber = "133")
@CardRegistration(set = "M20", collectorNumber = "130")
@CardRegistration(set = "A25", collectorNumber = "124")
@CardRegistration(set = "MM3", collectorNumber = "92")
@CardRegistration(set = "ARC", collectorNumber = "32")
@CardRegistration(set = "E01", collectorNumber = "43")
public class ChandrasOutrage extends Card {

    public ChandrasOutrage() {
        addSimultaneousSpellEffects(DealDamageEffect.toTargetCreature(4),
                DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), 2));
    }
}
