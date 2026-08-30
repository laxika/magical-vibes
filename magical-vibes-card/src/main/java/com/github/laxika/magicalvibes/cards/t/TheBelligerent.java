package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllowPlayFromTopOfLibraryUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;

import java.util.List;

@CardRegistration(set = "LCI", collectorNumber = "225")
@CardRegistration(set = "LCI", collectorNumber = "384")
public class TheBelligerent extends Card {

    public TheBelligerent() {
        addEffect(EffectSlot.ON_ATTACK, CreateTokenEffect.ofTreasureToken(1));
        addEffect(EffectSlot.ON_ATTACK, new AllowPlayFromTopOfLibraryUntilEndOfTurnEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));
    }
}
