package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.AllowPlayCardsFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "66")
@CardRegistration(set = "AFC", collectorNumber = "97")
public class DeadMansChest extends Card {

    public DeadMansChest() {
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.dies(ObjectRelation.ENCHANTED),
                        SequenceEffect.of(
                                ExileTopCardOfOwnLibraryEffect.of(Subjects.ownerOf(Subjects.triggering()),
                                        new TriggeringPermanentPower()),
                                AllowPlayCardsFromExileEffect.whileExiled(Subjects.cardsThisWay(StepKind.EXILE,
                                        ExileCardsFromTopUntilEffect.NONLAND)).withAnyManaType()));
    }
}
