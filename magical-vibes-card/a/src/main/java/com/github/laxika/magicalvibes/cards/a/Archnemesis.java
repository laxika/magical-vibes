package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.condition.OpponentAttacksWithAtLeastCreatures;
import com.github.laxika.magicalvibes.model.effect.AttachSourceAuraToTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingEnchantedPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "74")
@CardRegistration(set = "MSC", collectorNumber = "391")
public class Archnemesis extends Card {

    public Archnemesis() {
        setEnchantPlayer(true);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
        addTrigger(Triggers.youAttackPlayer(),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new HasAttacker(new PermanentIsAttackingEnchantedPlayerPredicate())),
                SequenceEffect.of(
                                new LoseLifeEffect(2, LoseLifeRecipient.DEFENDING_PLAYER),
                                new DrawCardEffect(1),
                                new GainLifeEffect(2)
                        ));
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new OpponentAttacksWithAtLeastCreatures(1, false)),
                new MayEffect(new AttachSourceAuraToTargetPlayerEffect(), "Attach Archnemesis to that player?"));
    }
}
