package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AddManaWhenLandOfColorTappedForManaEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectsToCounterBearersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "2")
@CardRegistration(set = "FIN", collectorNumber = "324")
@CardRegistration(set = "FIN", collectorNumber = "421")
public class UltimaOriginOfOblivion extends Card {

    public UltimaOriginOfOblivion() {
        var blightedLand = target(TargetFilters.land());
        blightedLand.addEffect(EffectSlot.ON_ATTACK,
                new PutCounterOnTargetPermanentEffect(CounterType.BLIGHT));
        blightedLand.addEffect(EffectSlot.ON_ATTACK,
                new GrantEffectsToCounterBearersEffect(CounterType.BLIGHT, List.of(
                        new LoseAllLandTypesEffect(GrantScope.ALL_LANDS, null),
                        new LosesAllAbilitiesEffect(GrantScope.ALL_LANDS),
                        new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.COLORLESS), GrantScope.ALL_LANDS)),
                        true));

        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddManaWhenLandOfColorTappedForManaEffect(ManaColor.COLORLESS));
    }
}
