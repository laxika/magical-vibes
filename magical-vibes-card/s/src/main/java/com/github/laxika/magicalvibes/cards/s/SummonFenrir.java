package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalCountersToCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedControllerSpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

@CardRegistration(set = "FIN", collectorNumber = "203")
@CardRegistration(set = "FIN", collectorNumber = "372")
public class SummonFenrir extends Card {

    public SummonFenrir() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.BATTLEFIELD_TAPPED));

        addEffect(EffectSlot.SAGA_CHAPTER_II, new RegisterDelayedControllerSpellCastTriggerEffect(
                new CardTypePredicate(CardType.CREATURE),
                List.of(new GrantAdditionalCountersToCastSpellEffect(new Fixed(1))), true, false));

        addEffect(EffectSlot.SAGA_CHAPTER_III, new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(), new DrawCardEffect(1)));
    }
}
