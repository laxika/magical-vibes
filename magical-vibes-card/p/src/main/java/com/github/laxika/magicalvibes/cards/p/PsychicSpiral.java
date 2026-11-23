package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.Set;

@CardRegistration(set = "RTR", collectorNumber = "47")
public class PsychicSpiral extends Card {

    public PsychicSpiral() {
        addSpellEffect(new EachPlayerShufflesZonesIntoLibraryEffect(Subjects.you(), Set.of(Zone.GRAVEYARD)));
        addSpellEffect(new MillEffect(AmountThisWay.shuffledAwayByYou(), Subjects.targetPlayer()));
    }
}
