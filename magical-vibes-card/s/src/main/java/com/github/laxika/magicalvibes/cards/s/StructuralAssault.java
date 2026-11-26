package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ArtifactsPutIntoGraveyardFromBattlefieldThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "SNC", collectorNumber = "126")
public class StructuralAssault extends Card {

    public StructuralAssault() {
        addSpellEffect(DestroyTargetPermanentEffect.all(new PermanentIsArtifactPredicate()));
        addSpellEffect(DealDamageEffect.toEachCreature(new ArtifactsPutIntoGraveyardFromBattlefieldThisTurn()));
    }
}
