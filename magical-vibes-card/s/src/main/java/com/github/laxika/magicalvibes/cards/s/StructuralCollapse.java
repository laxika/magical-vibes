package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesTwoDistinctPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "GTC", collectorNumber = "107")
public class StructuralCollapse extends Card {

    public StructuralCollapse() {
        addSpellEffect(new TargetPlayerSacrificesTwoDistinctPermanentsEffect(
                new PermanentIsArtifactPredicate(), new PermanentIsLandPredicate()));
        addSpellEffect(new DealDamageToPlayersEffect(2, DamageRecipient.TARGET_PLAYER));
    }
}
