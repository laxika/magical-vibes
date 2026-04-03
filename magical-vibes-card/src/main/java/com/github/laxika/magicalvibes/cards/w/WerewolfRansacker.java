package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndDamageControllerIfDestroyedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TwoOrMoreSpellsCastLastTurnConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

/**
 * Werewolf Ransacker — back face of Afflicted Deserter.
 * 5/4 Werewolf.
 * Whenever this creature transforms into Werewolf Ransacker, you may destroy target artifact.
 * If that artifact is put into a graveyard this way, Werewolf Ransacker deals 3 damage to
 * that artifact's controller.
 * At the beginning of each upkeep, if a player cast two or more spells last turn,
 * transform Werewolf Ransacker.
 */
public class WerewolfRansacker extends Card {

    public WerewolfRansacker() {
        // Target filter for the transform trigger: target artifact
        target(new PermanentPredicateTargetFilter(
                new PermanentIsArtifactPredicate(),
                "Target must be an artifact"
        ));

        // Whenever this creature transforms into Werewolf Ransacker, you may destroy target artifact.
        // If that artifact is put into a graveyard this way, deal 3 damage to that artifact's controller.
        addEffect(EffectSlot.ON_TRANSFORM_TO_BACK_FACE, new MayEffect(
                new DestroyTargetPermanentAndDamageControllerIfDestroyedEffect(3),
                "Destroy target artifact?"
        ));

        // At the beginning of each upkeep, if a player cast two or more spells last turn,
        // transform Werewolf Ransacker.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                new TwoOrMoreSpellsCastLastTurnConditionalEffect(new TransformSelfEffect()));
    }
}
