package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenFromHalfLifeTotalAndDealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsDamageToControllerEffect;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Snapshots the token size and delegates creation and subsequent damage to the shared handlers. */
@Component
public class CreateTokenFromHalfLifeTotalAndDealDamageEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenFromHalfLifeTotalAndDealDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var token = (CreateTokenFromHalfLifeTotalAndDealDamageEffect) effect;
        int x = Math.max(0, (gameData.getLife(entry.getControllerId()) + 1) / 2);
        entry.getCreatedPermanentIds().clear();
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                new CreateTokenEffect(token.tokenName(), x, x, token.color(), token.subtypes(), Set.of(), Set.of()),
                new TargetCreatureDealsDamageToControllerEffect(new Fixed(x),
                        GrantScope.TOKENS_CREATED_THIS_RESOLUTION, DamageRecipient.CONTROLLER)));
    }
}
