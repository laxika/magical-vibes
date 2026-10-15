package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantCardCharacteristicsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YECL", collectorNumber = "23")
public class PepRaucousRaider extends Card {

    public PepRaucousRaider() {
        ActivatedAbility artifactManaAbility = new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new AwardAnyColorManaEffect(3)
                ),
                "{T}, Sacrifice an artifact: Add three mana of any one color.");

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        null,
                        new ExileTopCardOfDamagedPlayerLibraryAndGrantCreatureControllerPlayPermissionWithPerpetualCharacteristicsEffect(
                                new PerpetuallyGrantCardCharacteristicsEffect(
                                        Set.of(CardType.ARTIFACT), Set.of(), List.of(artifactManaAbility)))));
        addActivatedAbility(artifactManaAbility);
    }
}
