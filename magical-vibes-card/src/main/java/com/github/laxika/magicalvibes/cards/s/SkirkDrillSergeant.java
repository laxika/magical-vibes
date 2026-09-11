package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardCreatureToBattlefieldElseGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "112")
@CardRegistration(set = "DD1", collectorNumber = "49")
@CardRegistration(set = "VMA", collectorNumber = "185")
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
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.GOBLIN), DRILL_SERGEANT_ABILITY));
        addEffect(EffectSlot.ON_DEATH, DRILL_SERGEANT_ABILITY);
    }
}
