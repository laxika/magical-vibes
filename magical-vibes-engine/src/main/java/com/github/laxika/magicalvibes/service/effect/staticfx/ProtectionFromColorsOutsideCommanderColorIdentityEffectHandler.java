package com.github.laxika.magicalvibes.service.effect.staticfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOutsideCommanderColorIdentityEffect;
import com.github.laxika.magicalvibes.service.effect.StaticBonusAccumulator;
import com.github.laxika.magicalvibes.service.effect.StaticEffectContext;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Component
public class ProtectionFromColorsOutsideCommanderColorIdentityEffectHandler implements StaticEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ProtectionFromColorsOutsideCommanderColorIdentityEffect.class;
    }

    @Override
    public void apply(StaticEffectContext context, CardEffect effect, StaticBonusAccumulator accumulator) {
        if (context.source() == null || context.target() == null
                || !context.source().isAttached()
                || !context.source().getAttachedTo().equals(context.target().getId())
                || context.sourceControllerId() == null) {
            return;
        }

        Set<CardColor> commanderIdentity = EnumSet.noneOf(CardColor.class);
        List<Card> commandZone = context.gameData().playerCommandZones
                .getOrDefault(context.sourceControllerId(), List.of());
        for (Card card : commandZone) {
            commanderIdentity.addAll(card.getColorIdentity());
        }

        context.gameData().playerBattlefields.forEach((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (!permanent.isCommander()) continue;
                UUID ownerId = permanent.getOriginalCard().getOwnerId();
                if (context.sourceControllerId().equals(ownerId)
                        || (ownerId == null && context.sourceControllerId().equals(controllerId))) {
                    commanderIdentity.addAll(permanent.getOriginalCard().getColorIdentity());
                }
            }
        });

        EnumSet<CardColor> protection = EnumSet.allOf(CardColor.class);
        protection.removeAll(commanderIdentity);
        accumulator.addProtectionColors(protection);
    }
}
