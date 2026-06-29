package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensPerCreatureCardInGraveyardEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreateTokensPerCreatureCardInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensPerCreatureCardInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CreateTokensPerCreatureCardInGraveyardEffect) effect;
        
                UUID controllerId = entry.getControllerId();
                List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
                int creatureCount = 0;
                if (graveyard != null) {
                    for (Card card : graveyard) {
                        if (card.hasType(CardType.CREATURE)) {
                            creatureCount++;
                        }
                    }
                }
                if (creatureCount <= 0) return;
                CreateTokenEffect tokenEffect = new CreateTokenEffect(
                        CardType.CREATURE, creatureCount, e.tokenName(), e.power(), e.toughness(),
                        e.color(), null, e.subtypes(), e.keywords(), e.additionalTypes(),
                        e.tappedAndAttacking(), false, Map.of(), List.of(), false, false, false, 0
                );
                permanentControlSupport.applyCreateToken(gameData, controllerId, tokenEffect, entry.getCard().getSetCode());
    
    }
}
