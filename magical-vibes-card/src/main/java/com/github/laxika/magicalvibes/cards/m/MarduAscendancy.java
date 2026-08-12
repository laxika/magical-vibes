package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;

import java.util.List;

@CardRegistration(set = "KTK", collectorNumber = "185")
public class MarduAscendancy extends Card {

    public MarduAscendancy() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringCardConditionalEffect(
                        new CardNotPredicate(new CardIsTokenPredicate()),
                        new CreateTokenEffect(1, "Goblin", 1, 1, CardColor.RED,
                                List.of(CardSubtype.GOBLIN), true)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new BoostAllOwnCreaturesEffect(0, 3)),
                "Sacrifice Mardu Ascendancy: Creatures you control get +0/+3 until end of turn."
        ));
    }
}
