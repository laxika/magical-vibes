package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ArtifactsPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "SNC", collectorNumber = "126")
public class StructuralAssault extends Card {

    public StructuralAssault() {
        addEffect(EffectSlot.SPELL, new DestroyAllPermanentsEffect(new PermanentIsArtifactPredicate()));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(
                new ArtifactsPutIntoGraveyardFromBattlefieldThisTurn(), false));
    }
}
