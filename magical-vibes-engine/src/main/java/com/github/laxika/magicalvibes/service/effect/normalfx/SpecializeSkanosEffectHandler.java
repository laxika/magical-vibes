package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.s.SkanosDragonVassal;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeSkanosEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies Skanos, Dragon Vassal's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeSkanosEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeSkanosEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CardColor color = ((SpecializeSkanosEffect) effect).color();
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Skanos, Dragon Vassal".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        SkanosDragonVassal.setSpecializedBaseCharacteristics(specialized);
        specialized.setPower(4);
        specialized.setToughness(4);
        SkanosDragonVassal.setFaceCharacteristics(specialized, color);
        SkanosDragonVassal.addAttackAbility(
                specialized, SkanosDragonVassal.specializedAttackEffect(color));
        source.exchangeCard(specialized);
    }
}
