package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.e.EldraziTemple;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.s.SpawningPit;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HandOfEmrakul.class, NestInvader.class, EldraziTemple.class, SpawningPit.class})
class HandOfEmrakulTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by sacrificing four Eldrazi Spawn without producing mana")
    void castsBySacrificingFourSpawnCreatures() {
        List<UUID> spawnIds = createEldraziSpawn(4);
        harness.setHand(player1, List.of(new HandOfEmrakul()));
        harness.castCreatureWithAlternateCost(player1, 0, spawnIds);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> spawnIds.contains(permanent.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotOnBattlefield(player1, "Hand of Emrakul");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hand of Emrakul");
    }

    @Test
    @DisplayName("Spawn without the Eldrazi subtype cannot pay the alternate cost")
    void rejectsNonEldraziSpawn() {
        Permanent pit = harness.addToBattlefieldAndReturn(player1, new SpawningPit());
        pit.setCounterCount(CounterType.CHARGE, 8);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        for (int i = 0; i < 4; i++) {
            harness.activateAbility(player1, 0, 1, null, null);
            harness.passBothPriorities();
        }
        List<UUID> spawnIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getId).toList();
        assertThat(spawnIds).hasSize(4);
        harness.setHand(player1, List.of(new HandOfEmrakul()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, spawnIds))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> spawnIds.contains(permanent.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Fewer than four Eldrazi Spawn cannot pay the alternate cost")
    void rejectsInsufficientSpawn() {
        List<UUID> spawnIds = createEldraziSpawn(3);
        harness.setHand(player1, List.of(new HandOfEmrakul()));

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, spawnIds))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> spawnIds.contains(permanent.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can be cast for nine mana without sacrificing permanents")
    void castsForMana() {
        harness.addToBattlefield(player1, new NestInvader());
        harness.castFromHand(player1, new HandOfEmrakul(), "{9}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hand of Emrakul");
        harness.assertOnBattlefield(player1, "Nest Invader");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Attacking makes the defending player sacrifice a permanent")
    void attackTriggersAnnihilatorOne() {
        addCreatureReady(player1, new HandOfEmrakul());
        harness.addToBattlefield(player2, new NestInvader());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Nest Invader");
        harness.assertInGraveyard(player2, "Nest Invader");
    }

    @Test
    @DisplayName("The defender may choose a land and sacrifices only one permanent")
    void defenderChoosesLand() {
        addCreatureReady(player1, new HandOfEmrakul());
        harness.addToBattlefield(player2, new NestInvader());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EldraziTemple());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(land.getId()));

        harness.assertInGraveyard(player2, "Eldrazi Temple");
        harness.assertOnBattlefield(player2, "Nest Invader");
        harness.assertOnBattlefield(player1, "Hand of Emrakul");
    }

    private List<UUID> createEldraziSpawn(int count) {
        for (int i = 0; i < count; i++) {
            harness.castFromHand(player1, new NestInvader(), "{1}{G}");
            resolveAllTriggers();
        }
        List<UUID> ids = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .map(Permanent::getId).toList();
        assertThat(ids).hasSize(count);
        return ids;
    }
}
