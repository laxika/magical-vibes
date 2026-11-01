package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantActivateAbilitiesOfGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCastSpellsFromZonesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "XLN", collectorNumber = "2")
public class AshesOfTheAbhorrent extends Card {

    public AshesOfTheAbhorrent() {
        // Players can't cast spells from graveyards or activate abilities of cards in graveyards.
        addStaticEffect(new PlayersCantCastSpellsFromZonesEffect(Set.of(Zone.GRAVEYARD)));
        addStaticEffect(new PlayersCantActivateAbilitiesOfGraveyardCardsEffect());

        // Whenever a creature dies, you gain 1 life.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANY), new GainLifeEffect(1));
    }
}
