package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaSpray.class, AvatarOfMight.class, GrizzlyBears.class,
        BoneSplinters.class, ChoMannoRevolutionary.class})
class MagmaSprayTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a small creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 2 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = harness.getPermanentId(player2, "Avatar of Might");
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(avatar.getMarkedDamage()).isEqualTo(2);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesSurvivorDestroyedLaterThisTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagmaSpray(), new BoneSplinters()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Avatar of Might"));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void exilesSurvivorSacrificedLaterThisTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new AvatarOfMight());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagmaSpray(), new BoneSplinters()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, sacrifice.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Avatar of Might");
        harness.assertNotInGraveyard(player1, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Avatar of Might"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void exilesCreatureEvenWhenAllSprayDamageWasPrevented() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagmaSpray(), new BoneSplinters()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Cho-Manno, Revolutionary");
        harness.assertNotInGraveyard(player2, "Cho-Manno, Revolutionary");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Cho-Manno, Revolutionary"));
    }

    @Test
    void replacementExpiresBeforeTheNextTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.setHand(player2, List.of(new BoneSplinters()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(target.getMarkedDamage()).isZero();
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorceryWithSacrifice(player2, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }
}
