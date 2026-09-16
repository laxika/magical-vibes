package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MB1", collectorNumber = "72")
public class DomesticatedMammoth extends Card {

    public DomesticatedMammoth() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateTokenAttachedToSourceEffect(pacifismToken()));
    }

    private static CreateTokenEffect pacifismToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Pacifism",
                0,
                0,
                CardColor.WHITE,
                Set.of(),
                List.of(CardSubtype.AURA),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, new EnchantedCreatureCantAttackOrBlockEffect()),
                List.of(),
                false,
                false,
                false,
                0,
                Set.of())
                .withTokenTargetFilter(TargetFilters.creature());
    }
}
