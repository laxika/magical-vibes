package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.g.Grindstone;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Verdigris.class, BottleGnomes.class, FightingDrake.class, Grindstone.class})
class VerdigrisTest extends BaseCardTest {

    @Test
    @DisplayName("Verdigris destroys a noncreature artifact and leaves other artifacts alone")
    void destroysOnlyTargetedNoncreatureArtifact() {
        harness.addToBattlefield(player2, new Grindstone());
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grindstone"));

        harness.assertNotOnBattlefield(player2, "Grindstone");
        harness.assertInGraveyard(player2, "Grindstone");
        harness.assertOnBattlefield(player2, "Bottle Gnomes");
        harness.assertNotInGraveyard(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Verdigris");
    }

    @Test
    @DisplayName("Sacrificing the target in response makes Verdigris fizzle")
    void targetCanBeSacrificedInResponse() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Bottle Gnomes"));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 23);
        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Verdigris");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Resolving Verdigris destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Verdigris");
    }

    @Test
    @DisplayName("Verdigris can destroy its controller's own artifact")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player1, "Bottle Gnomes");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Verdigris fizzles when target leaves before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new BottleGnomes());
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Bottle Gnomes");
        harness.castInstant(player1, 0, targetId);
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Verdigris");
    }

    @Test
    @DisplayName("Verdigris cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FightingDrake());
        harness.setHand(player1, List.of(new Verdigris()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID creatureId = harness.getPermanentId(player2, "Fighting Drake");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
