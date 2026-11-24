package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesZonesIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetPermanentIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "M11", collectorNumber = "206")
@CardRegistration(set = "M12", collectorNumber = "208")
@CardRegistration(set = "M13", collectorNumber = "204")
@CardRegistration(set = "M14", collectorNumber = "209")
@CardRegistration(set = "DDF", collectorNumber = "62")
@CardRegistration(set = "SLD", collectorNumber = "1634")
@CardRegistration(set = "C21", collectorNumber = "243")
public class ElixirOfImmortality extends Card {

    public ElixirOfImmortality() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new GainLifeEffect(5),
                        ShuffleTargetPermanentIntoLibraryEffect.of(Subjects.source()),
                        new EachPlayerShufflesZonesIntoLibraryEffect(Subjects.you(), Set.of(Zone.GRAVEYARD))),
                "{2}, {T}: You gain 5 life. Shuffle Elixir of Immortality and your graveyard into their owner's library."
        ));
    }
}
