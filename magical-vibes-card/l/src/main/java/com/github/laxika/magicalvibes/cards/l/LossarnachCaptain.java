package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "LTC", collectorNumber = "16")
@CardRegistration(set = "LTC", collectorNumber = "100")
@CardRegistration(set = "LTC", collectorNumber = "418")
public class LossarnachCaptain extends Card {

    public LossarnachCaptain() {
        // Whenever this creature or another Human you control enters, tap target creature an opponent controls.
        // The self half is unconditional: it must still trigger when a type-replacing effect makes this creature a non-Human.
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TapPermanentsEffect(TapUntapScope.TARGET))
                .addTrigger(Triggers.enters(
                                new PermanentAllOfPredicate(List.of(
                                        Triggers.CREATURE,
                                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN))),
                                ObjectRelation.ANOTHER_YOU_CONTROL),
                        new TapPermanentsEffect(TapUntapScope.TARGET));

        // At the beginning of your upkeep, create a 1/1 white Human Soldier creature token.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CreateTokenEffect(
                1,
                "Human Soldier",
                1,
                1,
                CardColor.WHITE,
                List.of(CardSubtype.HUMAN, CardSubtype.SOLDIER),
                Set.of(),
                Set.of()));
    }
}
