package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherControlledCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
class CopySpellForEachOtherControlledCreatureEffectHandlerTest {

    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private GameQueryService gameQueryService;
    @Mock private ValidTargetService validTargetService;
    private final CopySupport copySupport = new CopySupport();

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private CopySpellForEachOtherControlledCreatureEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        handler = new CopySpellForEachOtherControlledCreatureEffectHandler(
                gameBroadcastService, gameQueryService, validTargetService, copySupport);
    }

    @Test
    @DisplayName("Creates copies for each other creature the caster controls")
    void createsCopiesForOtherControlledCreatures() {
        Permanent dragon = createCreature("Mirrorwing Dragon");
        Permanent bears = createCreature("Grizzly Bears");
        Permanent oppBears = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).addAll(List.of(dragon, bears));
        gd.playerBattlefields.get(player2Id).add(oppBears);

        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(2);
        Card shock = createInstant("Shock");
        StackEntry snapshot = new StackEntry(StackEntryType.INSTANT_SPELL, shock, player1Id,
                "Shock", List.of(damage), 0, dragon.getId(), null, null, null, null, null);

        var effect = new CopySpellForEachOtherControlledCreatureEffect(
                snapshot, player1Id, dragon.getId());
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, dragon.getCard(), player1Id,
                "Mirrorwing Dragon's ability", List.of(effect));

        when(gameQueryService.isCreature(eq(gd), any())).thenReturn(true);
        when(validTargetService.canPermanentBeTargetedBySpell(eq(gd), any(), eq(shock), eq(player1Id)))
                .thenReturn(true);

        handler.resolve(gd, trigger, effect);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCopy()).isTrue();
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("No-op when spellSnapshot is null")
    void noOpWhenSnapshotNull() {
        var effect = new CopySpellForEachOtherControlledCreatureEffect();
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCreature("Mirrorwing Dragon").getCard(),
                player1Id, "trigger", List.of(effect));

        handler.resolve(gd, trigger, effect);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Skips spells that can't be copied")
    void skipsUncopyableSpells() {
        Permanent dragon = createCreature("Mirrorwing Dragon");
        Permanent bears = createCreature("Grizzly Bears");
        gd.playerBattlefields.get(player1Id).addAll(List.of(dragon, bears));

        Card shock = createInstant("Shock");
        shock.setCantBeCopied(true);
        StackEntry snapshot = new StackEntry(StackEntryType.INSTANT_SPELL, shock, player1Id,
                "Shock", List.of(new DealDamageToAnyTargetEffect(2)), 0, dragon.getId(),
                null, null, null, null, null);

        var effect = new CopySpellForEachOtherControlledCreatureEffect(
                snapshot, player1Id, dragon.getId());
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, dragon.getCard(), player1Id,
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
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        return perm;
    }
}
