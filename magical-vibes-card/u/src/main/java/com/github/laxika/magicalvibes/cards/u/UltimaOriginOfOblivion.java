package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaOfTypeProducedByTappedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "2")
@CardRegistration(set = "FIN", collectorNumber = "324")
@CardRegistration(set = "FIN", collectorNumber = "421")
public class UltimaOriginOfOblivion extends Card {

    public UltimaOriginOfOblivion() {
        var blightedLand = target(TargetFilters.land());
        blightedLand.addTrigger(Triggers.attacks(),
                new PutCountersEffect(CounterType.BLIGHT),
                new GrantEffectsToCounterBearersEffect(CounterType.BLIGHT, List.of(
                        new LoseAllLandTypesEffect(Subjects.otherLands(), null),
                        new LosesAllAbilitiesEffect(Subjects.otherLands()),
                        new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.COLORLESS), Subjects.otherLands())),
                        true));

        addTrigger(Triggers.youTapLandForMana(),
                new AddManaOfTypeProducedByTappedPermanentEffect(ManaColor.COLORLESS));
    }
}
