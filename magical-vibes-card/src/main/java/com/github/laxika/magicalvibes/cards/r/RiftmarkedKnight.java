package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "PLC", collectorNumber = "14")
public class RiftmarkedKnight extends Card {

    public RiftmarkedKnight() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK)));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE, new CreateTokenEffect(
                1,
                "Knight",
                2,
                2,
                CardColor.BLACK,
                List.of(CardSubtype.KNIGHT),
                Set.of(Keyword.FLANKING, Keyword.HASTE),
                Set.of(),
                Map.of(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.WHITE)))
        ));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}{W}",
                List.of(),
                "Suspend 3—{1}{W}{W}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHand(3));
    }
}
