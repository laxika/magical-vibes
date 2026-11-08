package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceRoomDoorUnlocked;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "137")
public class GlassworksShatteredYard extends Card {

    public GlassworksShatteredYard() {
        setRoomDoorManaCosts(List.of("{2}{R}", "{4}{R}"));

        addSpellEffect(new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Glassworks", List.of())
                        .withManaCost("{2}{R}"),
                new ChooseOneEffect.ChooseOneOption("Shattered Yard", List.of())
                        .withManaCost("{4}{R}")
        )));

        target(TargetFilters.creatureAnOpponentControls()).addTrigger(Triggers.doorUnlocked(0),
                new DealDamageToTargetCreatureEffect(4,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withEventCondition(new SourceRoomDoorUnlocked(1)),
                new DealDamageToPlayersEffect(1, DamageRecipient.EACH_OPPONENT));
    }
}
