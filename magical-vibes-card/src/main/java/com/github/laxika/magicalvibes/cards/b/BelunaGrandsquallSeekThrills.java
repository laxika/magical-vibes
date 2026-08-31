package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeekThrills;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CostModificationScope;
import com.github.laxika.magicalvibes.model.effect.ReduceCastCostForMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "WOE", collectorNumber = "220")
public class BelunaGrandsquallSeekThrills extends Card {

    public BelunaGrandsquallSeekThrills() {
        setBackFaceCard(new SeekThrills());
        addCastingOption(new AdventureCast("{2}{G}{U}{R}"));
        addEffect(EffectSlot.STATIC, new ReduceCastCostForMatchingSpellsEffect(
                new CardAllOfPredicate(List.of(
                        new CardIsPermanentPredicate(),
                        new CardHasAdventurePredicate())),
                1,
                CostModificationScope.SELF));
    }

    @Override
    public String getBackFaceClassName() {
        return "SeekThrills";
    }
}
