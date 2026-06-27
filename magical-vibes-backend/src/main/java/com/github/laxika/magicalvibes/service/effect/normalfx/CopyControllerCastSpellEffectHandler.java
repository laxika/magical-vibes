package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopyControllerCastSpellEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyControllerCastSpellEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopyControllerCastSpellEffect) effect;
        if (e.spellSnapshot() == null) return;

        StackEntry spellSnapshot = e.spellSnapshot();
        UUID castingPlayerId = e.castingPlayerId();
        Card spellCard = spellSnapshot.getCard();

        Card copyCard = copySupport.createCopyCard(spellCard);
        StackEntry copyEntry = copySupport.createCopyStackEntry(spellSnapshot, copyCard, castingPlayerId, spellSnapshot.getTargetId());

        gameData.stack.add(copyEntry);

        String logMsg = "A copy of " + spellCard.getName() + " is created.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - copy of {} created for controller", gameData.id, spellCard.getName());

        if (copyEntry.getTargetId() != null) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    castingPlayerId,
                    List.of(new CopySpellEffect()),
                    "Choose new targets for the copy of " + spellCard.getName() + "?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
