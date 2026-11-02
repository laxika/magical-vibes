package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DKA", collectorNumber = "135")
@CardRegistration(set = "SCD", collectorNumber = "224")
@CardRegistration(set = "DDQ", collectorNumber = "68")
@CardRegistration(set = "SLD", collectorNumber = "858")
@CardRegistration(set = "SIS", collectorNumber = "59")
@CardRegistration(set = "MIC", collectorNumber = "148")
public class DiregrafCaptain extends Card {

    public DiregrafCaptain() {
        // Deathtouch is applied automatically from the Scryfall keyword.

        // Other Zombie creatures you control get +1/+1.
        addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.ZOMBIE))));

        // Whenever another Zombie you control dies, target opponent loses 1 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
    }
}
