package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "BRC", collectorNumber = "9")
@CardRegistration(set = "BRC", collectorNumber = "56")
public class TerisiaresDevastation extends Card {

    public TerisiaresDevastation() {
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(new XValue(), LoseLifeRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.ofPowerstoneToken(new XValue()));

        // All creatures get -1/-1 until end of turn for each artifact you control.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(
                new Scaled(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), -1),
                new Scaled(new PermanentCount(new PermanentIsArtifactPredicate(), CountScope.CONTROLLER), -1)));
    }
}
