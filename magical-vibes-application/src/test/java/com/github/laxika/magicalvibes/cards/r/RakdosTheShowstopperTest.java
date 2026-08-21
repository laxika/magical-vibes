package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FetidImp;
import com.github.laxika.magicalvibes.cards.f.FoulImp;
import com.github.laxika.magicalvibes.cards.f.ForgeDevil;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player1, List.of(new RakdosTheShowstopper()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
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
