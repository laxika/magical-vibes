package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "THB", collectorNumber = "169")
public class DryadOfTheIlysianGrove extends Card {

    public DryadOfTheIlysianGrove() {
        addEffect(EffectSlot.STATIC, new PlaysAdditionalLandEachTurnEffect(1));

        for (CardSubtype basicLandType : List.of(CardSubtype.PLAINS, CardSubtype.ISLAND,
                CardSubtype.SWAMP, CardSubtype.MOUNTAIN, CardSubtype.FOREST)) {
            addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(basicLandType, GrantScope.OWN_LANDS));
        }

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(),
                GrantScope.OWN_PERMANENTS,
                new PermanentIsLandPredicate()
        ));
    }
}
