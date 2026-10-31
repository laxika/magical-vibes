package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SacrificedCardMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "NEO", collectorNumber = "171")
public class VoltageSurge extends Card {

    public VoltageSurge() {
        addCastingCost(SacrificePermanentCost.optional(new PermanentIsArtifactPredicate(), "an artifact"));
        SacrificedCardMatches artifactSacrificed = new SacrificedCardMatches(
                new CardTypePredicate(CardType.ARTIFACT), "an artifact");
        addSpellEffect(new ConditionalEffect(
                artifactSacrificed, new DealDamageToTargetCreatureOrPlaneswalkerEffect(4)));
        addSpellEffect(new ConditionalEffect(
                new NotCondition(artifactSacrificed), new DealDamageToTargetCreatureOrPlaneswalkerEffect(2)));
    }
}
