package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromMonocoloredEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsMulticoloredPredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "198")
public class GeneralFerrousRokiric extends Card {

    public GeneralFerrousRokiric() {
        addEffect(EffectSlot.STATIC, new ProtectionFromMonocoloredEffect());
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardIsMulticoloredPredicate(), List.of(
                        new CreateTokenEffect(
                                CardType.CREATURE, 1, "Golem", 4, 4,
                                CardColor.RED, Set.of(CardColor.RED, CardColor.WHITE),
                                List.of(CardSubtype.GOLEM), Set.of(), Set.of(CardType.ARTIFACT),
                                false, false, Map.of(), List.of(), false, false, false, 0, Set.of()))));
    }
}
