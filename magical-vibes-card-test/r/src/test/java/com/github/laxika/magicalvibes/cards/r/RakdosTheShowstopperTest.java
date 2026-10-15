package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CatacombCrocodile;
import com.github.laxika.magicalvibes.cards.c.ChanceEncounter;
import com.github.laxika.magicalvibes.cards.f.FetidImp;
import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.f.ForgeDevil;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SpawnOfMayhem;
import com.github.laxika.magicalvibes.cards.u.UnbreakableFormation;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosTheShowstopper.class, FoulImp.class, ForgeDevil.class, FetidImp.class,
        GrizzlyBears.class, HillGiant.class, SpawnOfMayhem.class, ChanceEncounter.class,
        CatacombCrocodile.class, UnbreakableFormation.class})
class RakdosTheShowstopperTest extends BaseCardTest {

    @Test
    @DisplayName("Rakdos does not flip for Demons, Devils, or Imps")
    void doesNotFlipForExcludedCreatureTypes() {
        harness.addToBattlefield(player2, new FoulImp());
        harness.addToBattlefield(player2, new ForgeDevil());
        harness.addToBattlefield(player2, new FetidImp());

        castRakdos();

        harness.assertOnBattlefield(player2, "Foul Imp");
        harness.assertOnBattlefield(player2, "Forge Devil");
        harness.assertOnBattlefield(player2, "Fetid Imp");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Rakdos, the Showstopper"));
    }

    @Test
    @DisplayName("Rakdos flips independently for each other creature and destroys losses")
    void flipsForEachOtherCreatureAndDestroysLosses() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());

        castRakdos();

        List<String> logs = gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Rakdos, the Showstopper"))
                .toList();
        assertThat(logs).hasSize(2);
        assertOutcomeMatchesZone(logs, "Grizzly Bears");
        assertOutcomeMatchesZone(logs, "Hill Giant");
    }

    private void castRakdos() {
        harness.castFromHand(player1, new RakdosTheShowstopper(), "{4}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Rakdos leaves other Demons and itself on the battlefield without flipping")
    void leavesDemonsOnBattlefield() {
        harness.addToBattlefield(player2, new SpawnOfMayhem());

        castRakdos();

        harness.assertOnBattlefield(player1, "Rakdos, the Showstopper");
        harness.assertOnBattlefield(player2, "Spawn of Mayhem");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .noneMatch(log -> log.contains("coin flip for Rakdos, the Showstopper"));
    }

    @Test
    @DisplayName("Heads and tails flips do not trigger Chance Encounter or affect noncreatures")
    void flipsHaveNoWinnerAndIgnoreNoncreatures() {
        harness.addToBattlefield(player1, new ChanceEncounter());
        for (int i = 0; i < 32; i++) {
            harness.addToBattlefield(player2, new CatacombCrocodile());
        }

        castRakdos();

        harness.assertOnBattlefield(player1, "Chance Encounter");
        harness.assertOnBattlefield(player1, "Rakdos, the Showstopper");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Rakdos, the Showstopper")))
                .hasSize(32);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Indestructible creatures still receive coin flips but survive tails")
    void indestructibleCreatureStillReceivesCoinFlip() {
        harness.addToBattlefield(player1, new CatacombCrocodile());
        harness.castFromHand(player1, new UnbreakableFormation(), "{2}{W}");
        harness.passBothPriorities();

        castRakdos();

        harness.assertOnBattlefield(player1, "Catacomb Crocodile");
        harness.assertNotInGraveyard(player1, "Catacomb Crocodile");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Rakdos, the Showstopper")))
                .hasSize(1);
    }

    private void assertOutcomeMatchesZone(List<String> logs, String creatureName) {
        String flipLog = logs.stream()
                .filter(log -> log.endsWith("for " + creatureName + "."))
                .findFirst()
                .orElseThrow();
        boolean lost = flipLog.contains(" loses the coin flip ");
        boolean onBattlefield = gd.playerBattlefields.values().stream()
                .flatMap(List::stream)
                .anyMatch(permanent -> permanent.getCard().getName().equals(creatureName));
        boolean inGraveyard = gd.playerGraveyards.values().stream()
                .flatMap(List::stream)
                .anyMatch(card -> card.getName().equals(creatureName));

        assertThat(onBattlefield != inGraveyard).isTrue();
        assertThat(inGraveyard).isEqualTo(lost);
    }
}
