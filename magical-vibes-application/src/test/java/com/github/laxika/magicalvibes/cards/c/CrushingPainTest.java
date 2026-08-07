package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrushingPainTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a creature that was dealt damage this turn")
    void killsDamagedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marks 6 damage on a creature that survives")
    void marksDamageOnSurvivor() {
        Permanent avatar = new Permanent(new AvatarOfMight());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(avatar);
        gd.permanentsDealtDamageThisTurn.add(avatar.getId());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, avatar.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
