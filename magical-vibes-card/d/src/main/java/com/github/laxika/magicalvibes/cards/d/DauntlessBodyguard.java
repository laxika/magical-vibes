package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "14")
public class DauntlessBodyguard extends Card {

    public DauntlessBodyguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ChooseAnotherCreatureOnEnterEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new GrantKeywordToChosenCreatureUntilEndOfTurnEffect(Keyword.INDESTRUCTIBLE, null)),
                "Sacrifice Dauntless Bodyguard: The chosen creature gains indestructible until end of turn."
        ));
    }
}
