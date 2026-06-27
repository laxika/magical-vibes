package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaAndSearchLibraryForCardNamedToBattlefieldEffect;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PayManaAndSearchLibraryForCardNamedToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayManaAndSearchLibraryForCardNamedToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (PayManaAndSearchLibraryForCardNamedToBattlefieldEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                                  PayManaAndSearchLibraryForCardNamedToBattlefieldEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (librarySearchSupport.isSearchPrevented(gameData, controllerId)) return;

        String playerName = gameData.playerIdToName.get(controllerId);
        ManaCost cost = new ManaCost(effect.manaCost());
        if (!cost.canPay(gameData.playerManaPools.get(controllerId))) {
            String logMsg = playerName + " can't pay " + effect.manaCost() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        cost.pay(gameData.playerManaPools.get(controllerId));

        librarySearchSupport.performLibrarySearch(
                gameData,
                controllerId,
                card -> effect.cardName().equals(card.getName()),
                effect.cardName(),
                "Search your library for a card named " + effect.cardName() + " and put it onto the battlefield.",
                false,
                true,
                LibrarySearchDestination.BATTLEFIELD
        );
    }
}
