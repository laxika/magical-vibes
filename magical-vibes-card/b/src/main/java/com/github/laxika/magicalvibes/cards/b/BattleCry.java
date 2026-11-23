package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterGlobalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "5")
public class BattleCry extends Card {

    public BattleCry() {
        // Untap all white creatures you control.
        addSpellEffect(new UntapPermanentsEffect(
                Subjects.controlledBy(Subjects.you(), new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentColorInPredicate(Set.of(CardColor.WHITE)))))));

        // Whenever a creature blocks this turn, it gets +0/+1 until end of turn.
        addSpellEffect(new RegisterGlobalTriggeredAbilityEffect(new TriggeredAbility(
                Triggers.creatureBlocks(), new BoostTargetCreatureEffect(new Fixed(0), new Fixed(1)))));
    }
}
