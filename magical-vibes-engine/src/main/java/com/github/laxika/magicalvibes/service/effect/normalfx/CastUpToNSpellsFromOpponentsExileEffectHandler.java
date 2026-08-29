package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastUpToNSpellsFromOpponentsExileEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Ashiok's capped free-cast choice over opponents' face-up exiled cards. */
@Component
@RequiredArgsConstructor
public class CastUpToNSpellsFromOpponentsExileEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastUpToNSpellsFromOpponentsExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int maxCount = ((CastUpToNSpellsFromOpponentsExileEffect) effect).maxCount();
        if (maxCount <= 0) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<UUID> castableSpellIds = new ArrayList<>();
        synchronized (gameData.exiledCards) {
            for (ExiledCardEntry exiled : gameData.exiledCards) {
                if (exiled.faceDown() || exiled.ownerId() == null
                        || exiled.ownerId().equals(controllerId) || !isSpell(exiled.card())) {
                    continue;
                }
                castableSpellIds.add(exiled.card().getId());
            }
        }

        if (castableSpellIds.isEmpty()) {
            return;
        }

        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.ImprovisationCapstoneCastChoice(
                        controllerId, castableSpellIds, Math.min(maxCount, castableSpellIds.size())));
    }

    private static boolean isSpell(Card card) {
        if (card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY)) {
            return true;
        }
        return card.getType().isPermanentType() && !card.hasType(CardType.LAND);
    }
}
