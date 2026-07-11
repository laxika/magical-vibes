package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RewindTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell and untaps up to four lands")
    void countersSpellAndUntapsLands() {
        // 4 tapped lands for player2 (the caster)
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        // Countered creature spell goes to owner's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        long untappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
        assertThat(untappedIslands).isEqualTo(4);
    }

    @Test
    @DisplayName("Only untaps up to four lands even if more are tapped")
    void untapsAtMostFourLands() {
        for (int i = 0; i < 6; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .forEach(Permanent::tap);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        long untappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(p -> !p.isTapped())
                .count();
        assertThat(untappedIslands).isEqualTo(4);

        long tappedIslands = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .filter(Permanent::isTapped)
                .count();
        assertThat(tappedIslands).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not untap non-land permanents")
    void doesNotUntapNonLandPermanents() {
        GrizzlyBears tappedBears = new GrizzlyBears();
        harness.addToBattlefield(player2, tappedBears);
        harness.addToBattlefield(player2, new Island());

        GameData gd = harness.getGameData();
        gd.playerBattlefields.get(player2.getId()).forEach(Permanent::tap);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Rewind()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        gd = harness.getGameData();
        Permanent island = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .findFirst().orElseThrow();
        assertThat(island.isTapped()).isFalse();

        Permanent creature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(creature.isTapped()).isTrue();
    }
}
