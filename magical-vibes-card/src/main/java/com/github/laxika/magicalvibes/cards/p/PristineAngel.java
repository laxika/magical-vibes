package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceUntapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "9")
public class PristineAngel extends Card {

    public PristineAngel() {
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new GrantEffectEffect(
                        new ProtectionFromCardTypesEffect(Set.of(CardType.ARTIFACT)), GrantScope.SELF)));
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceUntapped(),
                new ProtectionFromColorsEffect(Set.of(
                        CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN))));
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new MayEffect(
                new SpellCastTriggerEffect(null, List.of(new UntapPermanentsEffect(TapUntapScope.SELF))),
                "Untap Pristine Angel?"));
    }
}
