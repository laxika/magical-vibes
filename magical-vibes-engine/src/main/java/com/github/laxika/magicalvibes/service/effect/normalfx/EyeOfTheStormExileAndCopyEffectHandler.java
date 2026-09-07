package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EyeOfTheStormExileAndCopyEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EyeOfTheStormExileAndCopyEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final CopySupport copySupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EyeOfTheStormExileAndCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EyeOfTheStormExileAndCopyEffect eyeEffect = (EyeOfTheStormExileAndCopyEffect) effect;
        StackEntry originalSpell = gameData.stack.stream()
                .filter(stackEntry -> eyeEffect.originalSpellCardId().equals(stackEntry.getCard().getId()))
                .findFirst()
                .orElse(null);

        if (originalSpell != null && !originalSpell.isCopy()) {
            Card originalCard = originalSpell.getCard();
            gameData.stack.remove(originalSpell);
            exileService.exileCard(gameData, originalSpell.getOwnerId(), originalCard,
                    eyeEffect.sourcePermanentId());
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(eyeEffect.castingPlayerId()) + " exiles ",
                    originalCard, " (Eye of the Storm)."));
        }

        List<Card> exiledSpells = gameData.getCardsExiledByPermanent(eyeEffect.sourcePermanentId()).stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        List<UUID> copyIds = new ArrayList<>(exiledSpells.size());
        for (Card exiledSpell : exiledSpells) {
            Card copy = copySupport.createCopyCard(exiledSpell);
            exileService.exileCard(gameData, eyeEffect.castingPlayerId(), copy);
            copyIds.add(copy.getId());
        }

        if (!copyIds.isEmpty()) {
            interactionHandlerRegistry.begin(gameData,
                    new PendingInteraction.EyeOfTheStormCastChoice(
                            eyeEffect.castingPlayerId(), copyIds));
        }
        log.info("Game {} - Eye of the Storm created {} spell copies for {}",
                gameData.id, copyIds.size(), gameData.playerIdToName.get(eyeEffect.castingPlayerId()));
    }
}
