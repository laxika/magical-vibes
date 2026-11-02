package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "112")
@CardRegistration(set = "DD1", collectorNumber = "49")
@CardRegistration(set = "VMA", collectorNumber = "185")
@CardRegistration(set = "EVG", collectorNumber = "49")
public class SkirkDrillSergeant extends Card {

    private static final MayPayManaEffect DRILL_SERGEANT_ABILITY = new MayPayManaEffect(
            "{2}{R}",
            new RevealTopCardCreatureToBattlefieldElseGraveyardEffect(
                    new CardAllOfPredicate(List.of(
                            new CardIsPermanentPredicate(),
                            new CardSubtypePredicate(CardSubtype.GOBLIN)))),
            "Pay {2}{R}?");

    public SkirkDrillSergeant() {
        // Whenever this creature or another Goblin dies, you may pay {2}{R}. If you do, reveal the
        // top card of your library. If it's a Goblin permanent card, put it onto the battlefield.
        // Otherwise, put it into your graveyard.
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        ObjectRelation.THIS_OR_ANOTHER),
                DRILL_SERGEANT_ABILITY);
        // A noncreature Goblin permanent that dies is not a creature, so it reaches this card through the
        // ally and opponent permanent slots instead (CR 700.4: "dies" means put into a graveyard from the battlefield).
        TriggeringPermanentConditionalEffect noncreatureGoblinDies = new TriggeringPermanentConditionalEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.GOBLIN),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()))),
                DRILL_SERGEANT_ABILITY);
        addTrigger(Triggers.dies(null, ObjectRelation.ANOTHER_YOU_CONTROL), noncreatureGoblinDies);
        addTrigger(Triggers.dies(null, ObjectRelation.OPPONENT_CONTROLS), noncreatureGoblinDies);
    }
}
