package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CopySpellForEachOtherCreatureEffectHandlerTest {

    @Mock private GameLogService gameLogService;
    @Mock private GameQueryService gameQueryService;
    @Mock private ValidTargetService validTargetService;
    private final CopySupport copySupport = new CopySupport();

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CopySpellForEachOtherCreatureEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.addAll(List.of(player1Id, player2Id));
        gd.playerIds.addAll(List.of(player1Id, player2Id));
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        handler = new CopySpellForEachOtherCreatureEffectHandler(
                gameLogService, gameQueryService, validTargetService, copySupport);
    }

    @Test
    void createsCopiesForEveryOtherCreatureUnderTheAbilityController() {
        Permanent source = createCreature("Ink-Treader Nephilim");
        Permanent ownCreature = createCreature("Grizzly Bears");
        Permanent opposingCreature = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).addAll(List.of(source, ownCreature));
        gd.playerBattlefields.get(player2Id).add(opposingCreature);

        Card shock = createInstant("Shock");
        StackEntry snapshot = new StackEntry(StackEntryType.INSTANT_SPELL, shock, player2Id,
                "Shock", List.of(new DealDamageToAnyTargetEffect(2)), 0, source.getId(),
                null, null, null, null, null);
        var effect = new CopySpellForEachOtherCreatureEffect(snapshot, player2Id, source.getId());
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), player1Id,
                "Ink-Treader Nephilim's ability", List.of(effect));

        when(gameQueryService.isCreature(eq(gd), any())).thenReturn(true);
        when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shock), eq(player2Id)))
                .thenReturn(true);

        handler.resolve(gd, trigger, effect);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).extracting(StackEntry::getTargetId)
                .containsExactlyInAnyOrder(ownCreature.getId(), opposingCreature.getId());
        assertThat(gd.stack).allMatch(entry -> entry.getControllerId().equals(player1Id));
    }

    @Test
    void skipsUncopyableSpell() {
        Permanent source = createCreature("Ink-Treader Nephilim");
        Permanent otherCreature = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).addAll(List.of(source, otherCreature));

        Card shock = createInstant("Shock");
        shock.setCantBeCopied(true);
        StackEntry snapshot = new StackEntry(StackEntryType.INSTANT_SPELL, shock, player2Id,
                "Shock", List.of(new DealDamageToAnyTargetEffect(2)), 0, source.getId(),
                null, null, null, null, null);
        var effect = new CopySpellForEachOtherCreatureEffect(snapshot, player2Id, source.getId());
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, source.getCard(), player1Id,
                "trigger", List.of(effect));

        handler.resolve(gd, trigger, effect);

        assertThat(gd.stack).isEmpty();
    }

    private Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        return card;
    }

    private Permanent createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
