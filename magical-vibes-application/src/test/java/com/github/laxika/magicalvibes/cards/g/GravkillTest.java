package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LumenClassFrigate;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravkill.class, GrizzlyBears.class, LumenClassFrigate.class, MindStone.class})
class GravkillTest extends BaseCardTest {

    @Test
    void exilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void exilesTargetSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LumenClassFrigate());

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Lumen-Class Frigate");
        harness.assertNotInGraveyard(player2, "Lumen-Class Frigate");
    }

    @Test
    void cannotTargetPermanentThatIsNeitherCreatureNorSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MindStone());
        harness.setHand(player1, List.of(new Gravkill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or Spacecraft");
    }

    @Test
    void canExileYourOwnSpacecraft() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LumenClassFrigate());

        castAndResolve(target);

        harness.assertNotOnBattlefield(player1, "Lumen-Class Frigate");
        harness.assertNotInGraveyard(player1, "Lumen-Class Frigate");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(target.getCard().getId());
    }

    @Test
    void exilesSpacecraftThatIsAlsoACreatureOnlyOnce() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LumenClassFrigate());
        target.setCounterCount(CounterType.CHARGE, 12);

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Lumen-Class Frigate");
        harness.assertNotInGraveyard(player2, "Lumen-Class Frigate");
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .containsExactly(target.getCard().getId());
    }

    private void castAndResolve(Permanent target) {
        harness.setHand(player1, List.of(new Gravkill()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
