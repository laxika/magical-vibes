package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DancingScimitar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JayemdaeTome;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shatter.class, JayemdaeTome.class, DancingScimitar.class, GrizzlyBears.class})
class ShatterTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Shatter puts it on the stack with target")
    void castingPutsOnStack() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Shatter destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Jayemdae Tome");
        harness.assertInGraveyard(player2, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Can destroy own artifact with Shatter")
    void canDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player1, "Jayemdae Tome");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Jayemdae Tome");
        harness.assertInGraveyard(player1, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Can target an artifact creature with Shatter")
    void canTargetArtifactCreature() {
        harness.addToBattlefield(player2, new DancingScimitar());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Dancing Scimitar");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Dancing Scimitar");
        harness.assertInGraveyard(player2, "Dancing Scimitar");
    }

    @Test
    @DisplayName("Shatter goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = harness.getPermanentId(player2, "Jayemdae Tome");
        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Shatter");
    }

    @Test
    @DisplayName("Shatter fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, target));

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Shatter");
    }

    @Test
    @DisplayName("Shatter does not destroy an indestructible artifact")
    void indestructibleArtifactSurvives() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JayemdaeTome());
        artifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, artifact.getId());

        harness.assertOnBattlefield(player2, "Jayemdae Tome");
        harness.assertNotInGraveyard(player2, "Jayemdae Tome");
    }

    @Test
    @DisplayName("Cannot target a creature with Shatter")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }
}
