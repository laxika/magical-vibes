package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BileBlight;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VortexElemental.class, GrizzlyBears.class, Forest.class, BileBlight.class})
class VortexElementalTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability tucks Vortex Elemental and creatures blocking it")
    void tucksSourceAndCreaturesBlockingIt() {
        Permanent vortex = addCreatureReady(player1, new VortexElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vortex);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(vortex.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("The first ability tucks Vortex Elemental and creatures it is blocking")
    void tucksSourceAndCreaturesItIsBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent vortex = addCreatureReady(player2, new VortexElemental());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(vortex);
        assertThat(gd.playerDecks.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).contains(vortex.getCard());
    }

    @Test
    @DisplayName("The second ability requires the target creature to block Vortex Elemental")
    void targetMustBlockSource() {
        addCreatureReady(player1, new VortexElemental());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block");
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature permanent")
    void targetMustBeCreature() {
        addCreatureReady(player1, new VortexElemental());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The first ability works outside combat without moving unrelated creatures")
    void tucksOnlySourceOutsideCombat() {
        Permanent vortex = addCreatureReady(player1, new VortexElemental());
        Permanent unrelated = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vortex);
        assertThat(gd.playerDecks.get(player1.getId())).contains(vortex.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(unrelated);
    }

    @Test
    @DisplayName("The first ability moves every creature blocking Vortex Elemental")
    void tucksMultipleBlockers() {
        Permanent vortex = addCreatureReady(player1, new VortexElemental());
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vortex);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).contains(vortex.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).contains(first.getCard(), second.getCard());
    }

    @Test
    @DisplayName("Blockers are still moved if Vortex Elemental dies in response")
    void tucksBlockerAfterSourceDies() {
        Permanent vortex = addCreatureReady(player1, new VortexElemental());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new BileBlight()));

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, vortex.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(vortex.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(vortex.getCard());
    }

    @Test
    @DisplayName("The blocked attacker is still moved if Vortex Elemental dies in response")
    void tucksAttackerAfterSourceDies() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent vortex = addCreatureReady(player2, new VortexElemental());
        harness.setHand(player1, List.of(new BileBlight()));

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, vortex.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(vortex.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(vortex.getCard());
    }

    @Test
    @DisplayName("A tapped target is not forced to block and is not untapped")
    void tappedTargetCannotBlock() {
        addCreatureReady(player1, new VortexElemental());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(target.isTapped()).isTrue();
        assertThat(target.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("The target need not block another attacker when Vortex Elemental does not attack")
    void targetNeedNotBlockWhenSourceDoesNotAttack() {
        addCreatureReady(player1, new VortexElemental());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player1, List.of(1));
        gs.declareBlockers(gd, player2, List.of());

        assertThat(target.isBlocking()).isFalse();
    }
}
