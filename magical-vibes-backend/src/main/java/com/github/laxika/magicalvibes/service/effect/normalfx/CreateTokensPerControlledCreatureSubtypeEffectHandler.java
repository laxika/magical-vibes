package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerControlledCreatureSubtypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokensPerControlledCreatureSubtypeEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensPerControlledCreatureSubtypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensPerControlledCreatureSubtypeEffect) effect;
        
                int count = 0;
                List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
                if (battlefield != null) {
                    for (Permanent perm : battlefield) {
                        if (gameQueryService.isCreature(gameData, perm)
                                && perm.getCard().getSubtypes().contains(e.subtype())) {
                            count++;
                        }
                    }
                }
                int tokenCount = count / e.divisor();
                if (tokenCount <= 0) return;

                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        tokenCount, e.tokenName(), e.power(), e.toughness(),
                        e.color(), e.subtypes(), e.keywords(), e.additionalTypes()
                );
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(), tokenEffect, entry.getCard().getSetCode());
    
    }
}
