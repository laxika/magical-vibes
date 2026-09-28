package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrushingPain.class, AvatarOfMight.class, GrizzlyBears.class, Mountain.class, Shock.class})
class CrushingPainTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 6 damage to a creature that was dealt damage this turn")
    void killsDamagedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.permanentsDealtDamageThisTurn.add(bears.getId());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Marks 6 damage on a creature that survives")
    void marksDamageOnSurvivor() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        gd.permanentsDealtDamageThisTurn.add(avatar.getId());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertOnBattlefield(player2, "Avatar of Might");
        assertThat(avatar.getMarkedDamage()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a creature that was not dealt damage this turn")
    void cannotTargetUndamagedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent even if it was dealt damage this turn")
    void cannotTargetDamagedNoncreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        gd.permanentsDealtDamageThisTurn.add(mountain.getId());

        harness.setHand(player1, List.of(new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature damaged by another spell this turn")
    void canTargetCreatureDamagedByAnotherSpell() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());

        harness.setHand(player1, List.of(new Shock(), new CrushingPain()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        assertThat(avatar.getMarkedDamage()).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
    }
}
