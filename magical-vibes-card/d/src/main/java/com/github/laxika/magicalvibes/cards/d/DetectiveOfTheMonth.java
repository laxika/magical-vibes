package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "21")
@CardRegistration(set = "MKC", collectorNumber = "331")
public class DetectiveOfTheMonth extends Card {

    public DetectiveOfTheMonth() {
        addStaticEffect(new AscendEffect());
        addStaticEffect(new ConditionalEffect(new ControllerHasCityBlessing(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.ALL_OWN_CREATURES,
                        new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE))));
        addTrigger(Triggers.youDrawSecondCardEachTurn(),
                new CreateTokenEffect(1, "Detective", 2, 2, CardColor.WHITE,
                        java.util.Set.of(CardColor.WHITE, CardColor.BLUE),
                        java.util.List.of(CardSubtype.DETECTIVE)));
    }
}
