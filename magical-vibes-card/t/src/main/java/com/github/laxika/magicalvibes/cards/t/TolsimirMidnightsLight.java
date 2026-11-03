package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceAttackedThisCombat;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockTriggeringAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "236")
@CardRegistration(set = "MKM", collectorNumber = "371")
public class TolsimirMidnightsLight extends Card {

    public TolsimirMidnightsLight() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new CreateTokenEffect(
                CardType.CREATURE, 1, "Voja Fenstalker", 5, 5,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.WHITE),
                List.of(CardSubtype.WOLF), Set.of(Keyword.TRAMPLE), Set.of(), false, false,
                List.of(), List.of(), false, false, true, 0, Set.of()));

        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.creatureYouControlAttacks()
                                .withFilter(new PermanentHasSubtypePredicate(CardSubtype.WOLF)),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceAttackedThisCombat()),
                        new MustBlockTriggeringAttackerEffect());
    }
}
