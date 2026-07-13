package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FatalBlowTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature that was dealt damage this turn")
    void destroysCreatureDealtDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Destroyed creature cannot regenerate")
    void cannotRegenerate() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setRegenerationShield(1);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can target own creature that was dealt damage this turn")
    void canTargetOwnCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }
}
