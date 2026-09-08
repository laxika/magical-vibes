package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourcePowerCountersEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokenWithDyingSourcePowerCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenWithDyingSourcePowerCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int power = Math.max(0, entry.getEventValue());
        CreateTokenEffect template = ((CreateTokenWithDyingSourcePowerCountersEffect) effect).tokenTemplate();
        CreateTokenEffect resolved = new CreateTokenEffect(
                template.primaryType(), template.amount(), template.tokenName(), template.power(), template.toughness(),
                template.color(), template.colors(), template.subtypes(), template.keywords(), template.additionalTypes(),
                template.tappedAndAttacking(), template.tapped(), template.tokenEffects(), template.tokenAbilities(),
                template.exileAtEndOfCombat(), template.exileAtEndStep(), template.legendary(), power,
                template.grantedKeywordsUntilEndOfTurn());
        entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                gameData, entry.getControllerId(), resolved, entry.getCard().getSetCode()));
    }
}
