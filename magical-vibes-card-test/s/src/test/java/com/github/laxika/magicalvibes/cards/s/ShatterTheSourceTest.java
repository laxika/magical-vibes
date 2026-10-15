package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.f.FurnaceGremlin;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.u.UrnOfGodfire;
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

@CardUsed({UrnOfGodfire.class, SwordswornCavalier.class, FurnaceGremlin.class, InvasionOfInnistrad.class,
        ChandraHopesBeacon.class, ShatterTheSource.class})
class ShatterTheSourceTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode destroys a creature")
    void damageModeDestroysCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordswornCavalier());

        cast(0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Swordsworn Cavalier");
        harness.assertInGraveyard(player2, "Swordsworn Cavalier");
    }

    @Test
    @DisplayName("Damage mode removes loyalty from a planeswalker")
    void damageModeDamagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);

        cast(0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Damage mode removes defense counters from a battle")
    void damageModeDamagesBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
        battle.setCounterCount(CounterType.DEFENSE, 10);

        cast(0, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Artifact mode destroys an artifact")
    void artifactModeDestroysArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());

        cast(1, artifact.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urn of Godfire");
        harness.assertInGraveyard(player2, "Urn of Godfire");
    }

    @Test
    @DisplayName("Each mode rejects targets from the other mode")
    void modesRejectInvalidTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SwordswornCavalier());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());

        harness.setHand(player1, List.of(new ShatterTheSource()));
        addMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new ShatterTheSource()));
        addMana();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage mode cannot target a player")
    void damageModeRejectsPlayer() {
        harness.setHand(player1, List.of(new ShatterTheSource()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Six damage destroys a planeswalker with five loyalty")
    void damageModeDestroysPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        cast(0, planeswalker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chandra, Hope's Beacon");
        harness.assertInGraveyard(player2, "Chandra, Hope's Beacon");
    }

    @Test
    @DisplayName("Damage mode can convoke with a summoning-sick creature for generic mana")
    void damageModeUsesConvokeForGenericMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SwordswornCavalier());
        convoker.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SwordswornCavalier());
        harness.setHand(player1, List.of(new ShatterTheSource()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        gs.playCard(gd, player1, 0, 0, target.getId(), null, List.of(), List.of(convoker.getId()));
        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Swordsworn Cavalier");
        harness.assertOnBattlefield(player1, "Swordsworn Cavalier");
    }

    @Test
    @DisplayName("Artifact mode can convoke with a red creature for red mana")
    void artifactModeUsesConvokeForRedMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());
        harness.setHand(player1, List.of(new ShatterTheSource()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCard(gd, player1, 0, 1, artifact.getId(), null, List.of(), List.of(convoker.getId()));
        assertThat(convoker.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Urn of Godfire");
        harness.assertInGraveyard(player2, "Urn of Godfire");
    }

    @Test
    @DisplayName("A white creature cannot convoke for the red mana requirement")
    void wrongColorCannotPayRedMana() {
        Permanent convoker = harness.addToBattlefieldAndReturn(player1, new SwordswornCavalier());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new UrnOfGodfire());
        harness.setHand(player1, List.of(new ShatterTheSource()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 1, artifact.getId(), null,
                List.of(), List.of(convoker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Urn of Godfire");
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ShatterTheSource()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
