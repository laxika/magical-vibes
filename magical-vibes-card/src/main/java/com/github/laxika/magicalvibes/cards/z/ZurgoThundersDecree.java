package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeSacrificedEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "TDM", collectorNumber = "237")
public class ZurgoThundersDecree extends Card {

    public ZurgoThundersDecree() {
        addEffect(EffectSlot.ON_ATTACK,
                new CreateTokenEffect(2, "Warrior", 1, 1, CardColor.RED, List.of(CardSubtype.WARRIOR), true));
        addEffect(EffectSlot.ON_ATTACK, new SacrificeCreatedPermanentsAtEndStepEffect());
        addEffect(EffectSlot.STATIC, new GrantEffectEffect(new CantBeSacrificedEffect(), GrantScope.OWN_CREATURES,
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsTokenPredicate(), new PermanentHasSubtypePredicate(CardSubtype.WARRIOR)))));
    }
}
