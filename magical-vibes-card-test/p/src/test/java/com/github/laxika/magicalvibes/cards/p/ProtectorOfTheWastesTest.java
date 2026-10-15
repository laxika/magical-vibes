package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProtectorOfTheWastes.class, Bonesplitter.class, GloriousAnthem.class})
class ProtectorOfTheWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Protector of the Wastes exiles up to two artifacts or enchantments controlled by different players on ETB")
    void entersAndExilesTargetsControlledByDifferentPlayers() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();
        harness.castCreature(player1, 0, List.of(ownArtifact.getId(), opposingEnchantment.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Protector of the Wastes cannot target two permanents controlled by the same player")
    void cannotTargetTwoPermanentsControlledBySamePlayer() {
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0,
                List.of(firstArtifact.getId(), secondArtifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one permanent per controller");
    }

    @Test
    @DisplayName("Protector of the Wastes exiles targets when it becomes monstrous")
    void becomingMonstrousExilesTargets() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new ProtectorOfTheWastes());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opposingEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.handlePermanentChosen(player1, opposingEnchantment.getId());
        harness.passBothPriorities();

        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(protector.isMonstrous()).isTrue();
        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    void canEnterWithoutChoosingTargetsDespiteLegalTargets() {
        harness.addToBattlefield(player2, new Bonesplitter());
        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        harness.castCreature(player1, 0, List.of());
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player1.getId());
            resolveAllTriggers();
        }

        harness.assertOnBattlefield(player1, "Protector of the Wastes");
        harness.assertOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canExileOnlyOneTargetAndLeavesTheOtherAvailableTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        harness.castCreature(player1, 0, List.of(artifact.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(artifact.getCard().getId()));
        harness.assertNotInGraveyard(player1, "Bonesplitter");
    }

    @Test
    void cannotTargetAnOrdinaryCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ProtectorOfTheWastes());
        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBecomeMonstrousWhileSummoningSickAndDeclineExiling() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new ProtectorOfTheWastes());
        harness.addToBattlefield(player2, new Bonesplitter());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        assertThat(protector.isMonstrous()).isTrue();
        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canActivateMonstrosityAgainButDoesNotAddCountersOrTriggerAgain() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new ProtectorOfTheWastes());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();
        assertThat(protector.isMonstrous()).isTrue();
        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.addToBattlefield(player2, new Bonesplitter());
        addMonstrosityMana();
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multiplePendingMonstrosityActivationsOnlyAddCountersOnce() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new ProtectorOfTheWastes());
        addMonstrosityMana();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(protector.isMonstrous()).isTrue();
        assertThat(protector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canExileTwoArtifactsControlledByDifferentPlayers() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Bonesplitter());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        harness.setHand(player1, List.of(new ProtectorOfTheWastes()));
        addCastingMana();

        harness.castCreature(player1, 0, List.of(ownArtifact.getId(), opposingArtifact.getId()));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bonesplitter");
        harness.assertNotOnBattlefield(player2, "Bonesplitter");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(ownArtifact.getCard().getId()));
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(opposingArtifact.getCard().getId()));
    }

    private void addCastingMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
