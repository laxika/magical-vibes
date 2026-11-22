package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsFractionOfHandRoundedDownEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesFractionRoundedDownEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "SOS", collectorNumber = "94")
public class PoxPlague extends Card {

    public PoxPlague() {
        addSpellEffect(new LoseLifeEffect(new Divided(new ControllerLifeTotal(), 2), Subjects.eachPlayer()));
        addSpellEffect(new EachPlayerDiscardsFractionOfHandRoundedDownEffect(2));
        addSpellEffect(new EachPlayerSacrificesFractionRoundedDownEffect(2, new PermanentTruePredicate()));
    }
}
