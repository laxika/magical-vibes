package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "C15", collectorNumber = "30")
public class RiteOfTheRagingStorm extends Card {

    public RiteOfTheRagingStorm() {
        // Creatures named Lightning Rager can't attack this enchantment's controller or their planeswalkers.
        addEffect(EffectSlot.STATIC, new CreaturesCantAttackControllerUnlessPredicateEffect(
                new PermanentNotPredicate(new PermanentNamedPredicate("Lightning Rager")), true));

        // At the beginning of each player's upkeep, that player creates a Lightning Rager token.
        CreateTokenEffect lightningRager = new CreateTokenEffect(
                1, "Lightning Rager", 5, 1, CardColor.RED, List.of(CardSubtype.ELEMENTAL),
                Set.of(Keyword.TRAMPLE, Keyword.HASTE), Set.of(),
                Map.of(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect()));
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new CreateTokenForTriggeringPlayerEffect(lightningRager));
    }
}
