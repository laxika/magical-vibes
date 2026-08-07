package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ORI", collectorNumber = "79")
public class ThopterSpyNetwork extends Card {

    public ThopterSpyNetwork() {
        // "if you control an artifact" is an intervening-if (CR 603.4): checked as the upkeep
        // trigger would go on the stack and again on resolution.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentIsArtifactPredicate()),
                new CreateTokenEffect("Thopter", 1, 1,
                        null, List.of(CardSubtype.THOPTER),
                        Set.of(Keyword.FLYING), Set.of(CardType.ARTIFACT))));

        // Batched trigger: "one or more artifact creatures" is one event, so the draw happens once
        // per combat damage step per damaged player regardless of how many of them connected.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER, new AllyCombatDamageTriggerEffect(
                new PermanentAllOfPredicate(List.of(new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate())),
                new DrawCardEffect(1), false, true));
    }
}
