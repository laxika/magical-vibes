package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "289")
public class TezzeretsGatebreaker extends Card {

    public TezzeretsGatebreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(5,
                        new CardAnyOfPredicate(List.of(
                                new CardColorPredicate(CardColor.BLUE),
                                new CardTypePredicate(CardType.ARTIFACT)
                        ))));

        addActivatedAbility(new ActivatedAbility(true, "{5}{U}",
                List.of(new SacrificeSelfCost(), MakeAllCreaturesUnblockableEffect.ownCreatures()),
                "{5}{U}, {T}, Sacrifice this artifact: Creatures you control can't be blocked this turn."));
    }
}
