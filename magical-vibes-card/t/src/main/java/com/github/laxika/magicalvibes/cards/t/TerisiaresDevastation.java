package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "BRC", collectorNumber = "9")
@CardRegistration(set = "BRC", collectorNumber = "56")
public class TerisiaresDevastation extends Card {

    public TerisiaresDevastation() {
        addSpellEffect(new LoseLifeEffect(new XValue(), Subjects.you()));
        addSpellEffect(CreateTokenEffect.ofPowerstoneToken(new XValue()));

        // All creatures get -1/-1 until end of turn for each artifact you control.
        addSpellEffect(BoostEffect.allCreatures(
                new Scaled(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), -1),
                new Scaled(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), -1)));
    }
}
