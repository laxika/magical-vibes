package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.EarthSurge;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfDespair.class, EarthSurge.class, GruulSignet.class, GruulTurf.class})
class AngelOfDespairTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetArtifact() {
        harness.addToBattlefield(player2, new GruulSignet());
        castAngel(harness.getPermanentId(player2, "Gruul Signet"));

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player2, "Gruul Signet");
        harness.assertInGraveyard(player2, "Gruul Signet");
    }

    @Test
    void entersAndDestroysTargetLand() {
        harness.addToBattlefield(player2, new GruulTurf());
        castAngel(harness.getPermanentId(player2, "Gruul Turf"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Gruul Turf");
        harness.assertInGraveyard(player2, "Gruul Turf");
    }

    @Test
    void canDestroyOwnTargetPermanent() {
        harness.addToBattlefield(player1, new EarthSurge());
        castAngel(harness.getPermanentId(player1, "Earth Surge"));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Earth Surge");
        harness.assertInGraveyard(player1, "Earth Surge");
    }

    @Test
    void etbFizzlesIfTargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new GruulSignet());
        castAngel(harness.getPermanentId(player2, "Gruul Signet"));

        harness.passBothPriorities();
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    void canEnterWithoutTargetWhenNoPermanentsAreOnBattlefield() {
        harness.setHand(player1, List.of(new AngelOfDespair()));
        addAngelMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        assertThat(harness.getGameData().stack).isEmpty();
    }

    private void castAngel(UUID targetId) {
        harness.setHand(player1, List.of(new AngelOfDespair()));
        addAngelMana();
        harness.castCreature(player1, 0, targetId);
    }

    private void addAngelMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

}
