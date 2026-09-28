package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "WHO", collectorNumber = "174")
public class CybermanPatrol extends Card {

    public CybermanPatrol() {
        // Artifact creatures you control have afflict 3.
        addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                EffectSlot.ON_BECOMES_BLOCKED,
                new LoseLifeEffect(3, LoseLifeRecipient.DEFENDING_PLAYER),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentIsArtifactPredicate()));
    }
}
