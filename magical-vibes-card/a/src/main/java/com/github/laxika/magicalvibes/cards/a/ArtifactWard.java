package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventArtifactDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ATQ", collectorNumber = "3")
@CardRegistration(set = "ATQ", collectorNumber = "96")
public class ArtifactWard extends Card {

    public ArtifactWard() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentIsArtifactPredicate()))
                .addEffect(EffectSlot.STATIC, new PreventArtifactDamageToEnchantedCreatureEffect())
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        TargetingRestrictionEffect.fromAbilitySourceCardTypes(Set.of(CardType.ARTIFACT)),
                        GrantScope.ENCHANTED_CREATURE));
    }
}
