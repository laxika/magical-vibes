package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "128")
public class AlpineMoon extends Card {

    public AlpineMoon() {
        var chosenName = new PermanentHasSourceChosenNamePredicate();
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCardNameOnEnterEffect(List.of(), ChooseCardNameOnEnterEffect.HandAccess.NONE, true));
        addEffect(EffectSlot.STATIC, new LoseAllLandTypesEffect(GrantScope.OPPONENT_LANDS, chosenName));
        addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.OPPONENT_LANDS, chosenName));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapForAnyColor(), GrantScope.OPPONENT_LANDS, chosenName));
    }
}
