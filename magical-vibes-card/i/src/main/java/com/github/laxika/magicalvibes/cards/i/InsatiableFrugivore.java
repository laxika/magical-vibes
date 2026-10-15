package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenThenMayExileCardsAndRepeatEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeXPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "18")
@CardRegistration(set = "BLC", collectorNumber = "53")
public class InsatiableFrugivore extends Card {

    public InsatiableFrugivore() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenThenMayExileCardsAndRepeatEffect(CreateTokenEffect.ofFoodToken(1), 3));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new SacrificeXPermanentsCost(new PermanentHasSubtypePredicate(CardSubtype.FOOD)),
                        new BoostAllOwnCreaturesEffect(new XValue(), new Fixed(0)),
                        new GrantKeywordEffect(Keyword.MENACE, GrantScope.ALL_OWN_CREATURES)
                ),
                "{3}{B}, Sacrifice X Foods: Creatures you control get +X/+0 and gain menace until end of turn."
        ).withXValue());
    }
}
