package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Demolish.class, BottleGnomes.class, GrizzlyBears.class, Mountain.class, Millstone.class, Ornithopter.class})
class DemolishTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Demolish puts it on the stack with target")
    void castingPutsOnStack() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving destroys target land")
    void resolvingDestroysTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Resolving destroys target artifact")
    void resolvingDestroysTargetArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BottleGnomes()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player2, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Can destroy own artifact")
    void canDestroyOwnArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new BottleGnomes()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Bottle Gnomes");
    }

    @Test
    @DisplayName("Can destroy own land")
    void canDestroyOwnLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new Mountain()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    @DisplayName("Demolish goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Demolish");
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Mountain()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        // Demolish still goes to graveyard
        harness.assertInGraveyard(player1, "Demolish");
    }

    @Test
    @DisplayName("Can destroy an artifact creature")
    void canDestroyArtifactCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Ornithopter()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Regeneration prevents Demolish from destroying the target")
    void regenerationPreventsDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Millstone());
        target.setRegenerationShield(1);
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertNotInGraveyard(player2, "Millstone");
        assertThat(target.getRegenerationShield()).isZero();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot destroy a creature with Demolish")
    void cannotDestroyCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroys a noncreature artifact")
    void destroysNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Millstone()).getId();
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Millstone");
        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Sacrificing the target in response makes Demolish fizzle")
    void fizzlesWhenTargetSacrificedInResponse() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new BottleGnomes()).getId();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Demolish()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, targetId);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 23);
        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player2, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Demolish");
    }
}
