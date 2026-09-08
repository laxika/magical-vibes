package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfDespair.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class AngelOfDespairTest extends BaseCardTest {

    @Test
    void entersAndDestroysTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAngel(harness.getPermanentId(player2, "Grizzly Bears"));

        resolveAngelAndEtb();

        harness.assertOnBattlefield(player1, "Angel of Despair");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void entersAndDestroysTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        castAngel(harness.getPermanentId(player2, "Forest"));

        resolveAngelAndEtb();

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void canDestroyOwnTargetPermanent() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        castAngel(harness.getPermanentId(player1, "Glorious Anthem"));

        resolveAngelAndEtb();

        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        harness.assertInGraveyard(player1, "Glorious Anthem");
    }

    @Test
    void etbFizzlesIfTargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAngel(harness.getPermanentId(player2, "Grizzly Bears"));

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
        harness.passBothPriorities();

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

    private void resolveAngelAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
