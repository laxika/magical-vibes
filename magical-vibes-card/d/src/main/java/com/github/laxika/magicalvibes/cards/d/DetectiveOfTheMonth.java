package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "MKC", collectorNumber = "21")
@CardRegistration(set = "MKC", collectorNumber = "331")
public class DetectiveOfTheMonth extends Card {

    public DetectiveOfTheMonth() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE))));
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                        java.util.Set.of(CardColor.WHITE, CardColor.BLUE),
                        java.util.List.of(CardSubtype.DETECTIVE)));
    }
}
