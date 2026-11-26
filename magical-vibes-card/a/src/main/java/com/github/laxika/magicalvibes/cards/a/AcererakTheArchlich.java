package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCompletedDungeon;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.amount.PlayerCount;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.VentureIntoDungeonEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "87")
@CardRegistration(set = "SLD", collectorNumber = "1784")
public class AcererakTheArchlich extends Card {

    public AcererakTheArchlich() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new NotCondition(new ControllerHasCompletedDungeon(com.github.laxika.magicalvibes.model.Dungeon.TOMB_OF_ANNIHILATION))),
                SequenceEffect.of(ReturnToHandEffect.self(), new VentureIntoDungeonEffect()));
        addTrigger(Triggers.attacks(),
                // "For each opponent, you create a 2/2 black Zombie creature token unless that player sacrifices a
                // creature of their choice": every opponent chooses in turn order, then the sacrifices and the tokens
                // (its ruling).
                UnlessPaysEffect.eachUnless(Subjects.eachOpponent(),
                        new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                        opponents -> CreateTokenEffect.blackZombie(1).withAmount(new PlayerCount(opponents)))
                        .payingTogether());
    }
}
