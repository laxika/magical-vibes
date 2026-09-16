package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCardsInGraveyardEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.springframework.stereotype.Component;

@Component
public class CreateTokenCardsInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    public CreateTokenCardsInGraveyardEffectHandler(GraveyardService graveyardService) {
        this.graveyardService = graveyardService;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCardsInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var create = (CreateTokenCardsInGraveyardEffect) effect;
        if (create.amount() == 0 || gameData.playerGraveyards.get(entry.getControllerId()) == null) {
            return;
        }

        String sourceSetCode = entry.getCard() == null ? null : entry.getCard().getSetCode();
        for (int i = 0; i < create.amount(); i++) {
            Card tokenCard = TokenCardFactory.create(
                    create.tokenTemplate(),
                    create.tokenTemplate().tokenPower(),
                    create.tokenTemplate().tokenToughness(),
                    sourceSetCode);
            tokenCard.setManaCost(create.manaCost());
            tokenCard.setCardText(create.cardText());
            tokenCard.setOwnerId(entry.getControllerId());
            tokenCard.setTokenCard(true);
            for (var ability : create.graveyardActivatedAbilities()) {
                tokenCard.addGraveyardActivatedAbility(ability);
            }
            tokenCard.freeze();
            graveyardService.addCardToGraveyard(gameData, entry.getControllerId(), tokenCard);
        }
    }
}
