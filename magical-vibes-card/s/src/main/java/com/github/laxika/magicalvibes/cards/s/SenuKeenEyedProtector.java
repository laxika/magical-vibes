package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSelfCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromExileToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "8")
@CardRegistration(set = "ACR", collectorNumber = "128")
public class SenuKeenEyedProtector extends Card {

    public SenuKeenEyedProtector() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{W}",
                List.of(new ExileSelfCost(), new GainLifeEffect(2), new ScryEffect(2)),
                "{T}, Exile Senu: You gain 2 life and scry 2."
        ));

        addEffect(EffectSlot.EXILE_ON_ALLY_CREATURE_ATTACKS_UNBLOCKED,
                new TriggeringCardConditionalEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new ReturnSourceCardFromExileToBattlefieldEffect(false, true)));
    }
}
