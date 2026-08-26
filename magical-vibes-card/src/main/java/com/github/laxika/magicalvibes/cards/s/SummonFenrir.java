package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EmpowerNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "FIN", collectorNumber = "203")
public class SummonFenrir extends Card {

    public SummonFenrir() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new EmpowerNextCreatureSpellThisTurnEffect(false, 1));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(), new DrawCardEffect(1)));
    }
}
