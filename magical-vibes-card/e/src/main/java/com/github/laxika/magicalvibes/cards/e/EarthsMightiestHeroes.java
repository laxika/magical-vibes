package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TeamworkCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.TeamworkCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MSH", collectorNumber = "165")
public class EarthsMightiestHeroes extends Card {

    public EarthsMightiestHeroes() {
        addEffect(EffectSlot.SPELL, new TeamworkCost(5));

        CardTypePredicate creature = new CardTypePredicate(CardType.CREATURE);
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new TeamworkCostPaid(),
                new LookAtTopCardsEffect(
                        new Fixed(8), new Fixed(1), creature,
                        LookDestination.GRAVEYARD, true,
                        LibrarySearchDestination.BATTLEFIELD, true),
                new LookAtTopCardsEffect(
                        new Fixed(8), new Fixed(8), creature,
                        LookDestination.GRAVEYARD, true,
                        LibrarySearchDestination.BATTLEFIELD, true)
        ));
    }
}
