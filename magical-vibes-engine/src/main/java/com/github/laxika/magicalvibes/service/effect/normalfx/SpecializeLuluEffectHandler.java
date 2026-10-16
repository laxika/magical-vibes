package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.l.LuluForgetfulHollyphant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SpecializeLuluEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Applies Lulu, Forgetful Hollyphant's five digital specialized faces. */
@Component
@RequiredArgsConstructor
public class SpecializeLuluEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SpecializeLuluEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SpecializeLuluEffect specialize = (SpecializeLuluEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null || !"Lulu, Forgetful Hollyphant".equals(source.getCard().getName())) {
            return;
        }

        Card specialized = source.getCard().createRuntimeCopy();
        specialized.clearRulesTextAndAbilities();
        LuluForgetfulHollyphant.setSpecializedBaseCharacteristics(specialized);
        LuluForgetfulHollyphant.setFaceCharacteristics(specialized, specialize.color());
        LuluForgetfulHollyphant.addAttackTrigger(specialized, specialize.color());
        source.exchangeCard(specialized);
    }
}
