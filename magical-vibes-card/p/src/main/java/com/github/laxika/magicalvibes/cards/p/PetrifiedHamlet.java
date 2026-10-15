package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilitiesOfChosenNameCantBeActivatedEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenNamePredicate;

@CardRegistration(set = "SOS", collectorNumber = "259")
public class PetrifiedHamlet extends Card {

    public PetrifiedHamlet() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseCardNameOnEnterEffect(CardType.LAND));
        addEffect(EffectSlot.STATIC, new ActivatedAbilitiesOfChosenNameCantBeActivatedEffect());
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.COLORLESS),
                GrantScope.ALL_LANDS_INCLUDING_SELF,
                new PermanentHasSourceChosenNamePredicate()));
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));
    }
}
