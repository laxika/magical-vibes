package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WorldspineWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Galvanize.class, WorldspineWurm.class, GrizzlyBears.class})
class GalvanizeTest extends BaseCardTest {

    @Test
    void dealsThreeDamageWithoutTwoCardsDrawn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageAfterDrawingTwoCards() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        draw(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void dealsThreeDamageAfterDrawingOnlyOneCard() {
        harness.setLibrary(player1, List.of(new Galvanize()));
        draw(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void dealsFiveDamageAfterDrawingMoreThanTwoCards() {
        harness.setLibrary(player1, List.of(new Galvanize(), new Galvanize(), new Galvanize()));
        draw(player1);
        draw(player1);
        draw(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void opponentsDrawsDoNotIncreaseDamage() {
        harness.setLibrary(player2, List.of(new Galvanize(), new Galvanize()));
        draw(player2);
        draw(player2);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());

        castGalvanize(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void checksDrawCountAtResolution() {
        harness.setLibrary(player1, List.of(new Galvanize(), new Galvanize()));
        draw(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WorldspineWurm());
        harness.setHand(player1, List.of(new Galvanize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());

        draw(player1);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(5);
    }

    @Test
    void canTargetAndKillControllersCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castGalvanize(target);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Galvanize");
    }

    @Test
    void cannotTargetAPlayer() {
        harness.setHand(player1, List.of(new Galvanize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castGalvanize(Permanent target) {
        harness.setHand(player1, List.of(new Galvanize()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void draw(Player player) {
        harness.inMutationScope(() -> {
            harness.getDrawService().resolveDrawCard(gd, player.getId());
            harness.getPlayerInputService().processNextMayAbility(gd);
        });
    }
}
