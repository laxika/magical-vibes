package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LibraryNinjutsuEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.CloneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the battlefield half of a library-ninjutsu ability. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LibraryNinjutsuEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final CloneService cloneService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LibraryNinjutsuEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LibraryNinjutsuEffect libraryNinjutsu = (LibraryNinjutsuEffect) effect;
        UUID controllerId = entry.getControllerId();
        Card card = entry.getCard();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (card == null || library == null || library.stream().noneMatch(candidate -> candidate.getId().equals(card.getId()))) {
            return;
        }
        library.removeIf(candidate -> candidate.getId().equals(card.getId()));

        if (cloneService.prepareNinjutsuCloneReplacementEffect(
                gameData, controllerId, card, libraryNinjutsu.attackTargetId())) {
            return;
        }

        Permanent permanent = new Permanent(card, Zone.LIBRARY);
        permanent.tap();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, permanent);
        permanent.setAttacking(true);
        permanent.setAttackTarget(libraryNinjutsu.attackTargetId());

        gameLogService.append(gameData, GameLog.cardThen(card, " enters the battlefield tapped and attacking."));
        log.info("Game {} - {} enters tapped and attacking via library ninjutsu", gameData.id, card.getName());
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, controllerId, card, null, false);
    }
}
