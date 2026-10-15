package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DraftProteanWarEngineSpellbookEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YDMU", collectorNumber = "25")
public class ProteanWarEngine extends Card {

    public ProteanWarEngine() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DraftProteanWarEngineSpellbookEffect());
        addEffect(EffectSlot.ON_SELF_BECOMES_CREWED,
                new BecomeCopyOfExiledCreatureWithSourceUntilEndOfTurnEffect(
                        Set.of(CardType.ARTIFACT), Set.of(CardSubtype.VEHICLE)));
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"));
    }
}
