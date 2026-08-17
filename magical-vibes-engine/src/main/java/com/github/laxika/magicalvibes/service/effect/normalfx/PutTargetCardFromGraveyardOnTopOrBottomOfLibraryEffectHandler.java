package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect) effect;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        if (e.destination() == PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.CHOOSE) {
            playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                    new ChooseOneEffect(List.of(
                            new ChooseOneEffect.ChooseOneOption("Top",
                                    new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                                            PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.TOP)),
                            new ChooseOneEffect.ChooseOneOption("Bottom",
                                    new PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect(
                                            PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.BOTTOM))
                    )));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, targetCardId);
        GraveyardChoiceDestination destination = e.destination()
                == PutTargetCardFromGraveyardOnTopOrBottomOfLibraryEffect.Destination.TOP
                ? GraveyardChoiceDestination.TOP_OF_OWNERS_LIBRARY
                : GraveyardChoiceDestination.BOTTOM_OF_OWNERS_LIBRARY;
        graveyardReturnSupport.moveCardToDestination(gameData, ownerId, targetCard, destination,
                null, null, false);
    }
}
