package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.a.AvariceTotem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrarkClanOgre.class, AvariceTotem.class, Arachnoid.class})
class KrarkClanOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability pays red mana, sacrifices an artifact, and uses the target creature")
    void activatesAndMakesTargetUnableToBlock() {
        Permanent ogre = addCreatureReady(player1, new KrarkClanOgre());
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Avarice Totem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        ogre.setAttacking(true);
        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new KrarkClanOgre());
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvariceTotem());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Avarice Totem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability cannot be activated without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addCreatureReady(player1, new KrarkClanOgre());
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Arachnoid");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ogre can activate the ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent ogre = harness.addToBattlefieldAndReturn(player1, new KrarkClanOgre());
        ogre.setSummoningSick(true);
        ogre.tap();
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(ogre.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Avarice Totem");
    }

    @Test
    @DisplayName("The Ogre can target itself, and the restriction expires after the turn")
    void canTargetItselfAndRestrictionExpires() {
        Permanent ogre = addCreatureReady(player1, new KrarkClanOgre());
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent attacker = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, ogre.getId());
        harness.passBothPriorities();

        assertThat(ogre.isCantBlockThisTurn()).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        attacker.setAttacking(true);
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        assertThat(ogre.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("An artifact creature can be both the target and the sacrificed artifact")
    void canSacrificeItsOwnTarget() {
        addCreatureReady(player1, new KrarkClanOgre());
        Permanent artifactCreature = addCreatureReady(player1, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, artifactCreature.getId());

        harness.assertInGraveyard(player1, "Arachnoid");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Krark-Clan Ogre");
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        addCreatureReady(player1, new KrarkClanOgre());
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Arachnoid");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The sacrifice cost cannot be paid without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new KrarkClanOgre());
        harness.addToBattlefield(player1, new AvariceTotem());
        Permanent target = addCreatureReady(player2, new Arachnoid());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Avarice Totem");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
