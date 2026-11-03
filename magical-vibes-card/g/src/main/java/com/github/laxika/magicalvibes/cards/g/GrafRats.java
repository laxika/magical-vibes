package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.MeldWithNamedCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentOwnedBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "113")
@CardRegistration(set = "EMN", collectorNumber = "91")
@CardRegistration(set = "SIR", collectorNumber = "115")
public class GrafRats extends Card {

    private static final String PARTNER_NAME = "Midnight Scavengers";

    public GrafRats() {
        setBackFaceCard(new ChitteringHost());

        // At the beginning of combat on your turn, if you both own and control this creature and a
        // creature named Midnight Scavengers, exile them, then meld them into Chittering Host.
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentIsSourceCardPredicate(),
                                        new PermanentOwnedBySourceControllerPredicate()))),
                                new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                        new PermanentNamedPredicate(PARTNER_NAME),
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentOwnedBySourceControllerPredicate())))))),
                new MeldWithNamedCreatureEffect(PARTNER_NAME));
    }

    @Override
    public String getBackFaceClassName() {
        return "ChitteringHost";
    }
}
