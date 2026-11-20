package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.TopCardOfLibraryType;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryOwner;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "94")
public class Paroxysm extends Card {

    public Paroxysm() {
        target(TargetFilters.creature()).addTrigger(Triggers.upkeep(ActivePlayer.ENCHANTED_PERMANENT_CONTROLLER),
                SequenceEffect.of(
                        new RevealTopCardOfLibraryEffect(Subjects.triggeringPlayer()),
                        new ConditionalReplacementEffect(
                                new TopCardOfLibraryType(CardType.LAND, LibraryOwner.ENCHANTED_PERMANENT_CONTROLLER),
                                new BoostSelfEffect(Subjects.attachedAtTrigger(), 3, 3),
                                new DestroyTargetPermanentEffect(Subjects.attachedAtTrigger()))));
    }
}
