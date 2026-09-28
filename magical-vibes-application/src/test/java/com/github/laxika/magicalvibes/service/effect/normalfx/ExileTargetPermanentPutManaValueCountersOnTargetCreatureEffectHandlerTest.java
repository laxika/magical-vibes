package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffectHandlerTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameLogService gameLogService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PermanentCounterSupport permanentCounterSupport;

    private GameData gameData;
    private UUID controllerId;
    private ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffectHandler handler;

    @BeforeEach
    void setUp() {
        controllerId = UUID.randomUUID();
        gameData = new GameData(UUID.randomUUID(), "test", controllerId, "Player");
        handler = new ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffectHandler(
                gameQueryService, gameLogService, permanentRemovalService, permanentCounterSupport);
    }

    @Test
    void exilesPermanentAndUsesItsManaValueForCounters() {
        Card sourceCard = card("Ruinous Intrusion", CardType.INSTANT, null);
        Card artifactCard = card("Artifact", CardType.ARTIFACT, "{3}");
        Card creatureCard = card("Creature", CardType.CREATURE, "{1}{G}");
        Permanent artifact = new Permanent(artifactCard);
        Permanent creature = new Permanent(creatureCard);
        var effect = new ExileTargetPermanentPutManaValueCountersOnTargetCreatureEffect();
        StackEntry entry = new StackEntry(
                StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                List.of(effect), 0, List.of(artifact.getId(), creature.getId()));

        when(gameQueryService.findPermanentById(gameData, artifact.getId())).thenReturn(artifact);
        when(gameQueryService.findPermanentById(gameData, creature.getId())).thenReturn(creature);
        when(gameQueryService.isCreature(gameData, creature)).thenReturn(true);
        when(gameQueryService.findPermanentController(gameData, creature.getId())).thenReturn(controllerId);

        handler.resolve(gameData, entry, effect);

        verify(permanentRemovalService).removePermanentToExile(gameData, artifact);
        verify(permanentRemovalService).removeOrphanedAuras(gameData);
        verify(permanentCounterSupport).applyPlusOnePlusOneCounters(gameData, entry, creature, 3);
    }

    private Card card(String name, CardType type, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        if (manaCost != null) {
            card.setManaCost(manaCost);
        }
        return card;
    }
}
