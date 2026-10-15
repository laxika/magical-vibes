package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.r.RakdosLocket;
import com.github.laxika.magicalvibes.cards.s.SteepleCreeper;
import com.github.laxika.magicalvibes.cards.s.StonyStrength;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrotesqueDemise.class, AxebaneBeast.class, SteepleCreeper.class,
        StonyStrength.class, RakdosLocket.class, ConcordiaPegasus.class})
class GrotesqueDemiseTest extends BaseCardTest {

    private void giveGrotesqueDemise() {
        harness.setHand(player1, List.of(new GrotesqueDemise()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exiles the targeted creature with power 3 or less")
    void exilesTargetCreature() {
        Permanent target = addCreatureReady(player2, new AxebaneBeast());
        giveGrotesqueDemise();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Axebane Beast");
        harness.assertNotInGraveyard(player2, "Axebane Beast");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Axebane Beast"));
    }

    @Test
    @DisplayName("Cannot target a creature with power greater than 3")
    void cannotTargetHighPowerCreature() {
        Permanent target = addCreatureReady(player2, new SteepleCreeper());
        giveGrotesqueDemise();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    @DisplayName("Can target a creature with exactly power 3")
    void canTargetPowerThreeCreature() {
        Permanent target = addCreatureReady(player2, new AxebaneBeast());
        giveGrotesqueDemise();

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new AxebaneBeast());
        giveGrotesqueDemise();

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Axebane Beast"));
        harness.assertInGraveyard(player1, "Grotesque Demise");
    }

    @Test
    @DisplayName("Does not exile a creature whose power rises above 3 in response")
    void fizzlesIfTargetPowerIncreases() {
        Permanent target = addCreatureReady(player2, new AxebaneBeast());
        giveGrotesqueDemise();
        harness.setHand(player2, List.of(new StonyStrength()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Axebane Beast");
        assertThat(gd.exiledCards).isEmpty();
        harness.assertInGraveyard(player1, "Grotesque Demise");
    }

    @Test
    @DisplayName("Can exile a creature controlled by the caster")
    void exilesOwnCreature() {
        Permanent target = addCreatureReady(player1, new AxebaneBeast());
        giveGrotesqueDemise();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player1, "Axebane Beast");
        harness.assertNotInGraveyard(player1, "Axebane Beast");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Axebane Beast"));
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RakdosLocket());
        giveGrotesqueDemise();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Rakdos Locket");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles a creature whose power is below 3")
    void exilesLowPowerCreature() {
        Permanent target = addCreatureReady(player2, new ConcordiaPegasus());
        giveGrotesqueDemise();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Concordia Pegasus");
        harness.assertNotInGraveyard(player2, "Concordia Pegasus");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Concordia Pegasus"));
    }
}
