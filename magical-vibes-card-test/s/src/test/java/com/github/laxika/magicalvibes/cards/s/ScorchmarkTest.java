package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.c.ChoMannoRevolutionary;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
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

@CardUsed({Scorchmark.class, GrizzlyBears.class, AvatarOfMight.class, Terror.class, Unsummon.class,
        ChoMannoRevolutionary.class})
class ScorchmarkTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Deals 2 damage to a surviving creature and marks it for exile if it dies this turn")
    void marksSurvivorForExile() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        UUID targetId = avatar.getId();
        harness.setHand(player1, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        assertThat(avatar.getMarkedDamage()).isEqualTo(2);
        assertThat(avatar.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiles a surviving target destroyed later in the same turn")
    void exilesWhenDestroyedLaterThisTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Scorchmark(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.assertOnBattlefield(player2, "Avatar of Might");
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertNotInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Exile replacement expires when the turn ends")
    void replacementExpiresAtEndOfTurn() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Scorchmark()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.setHand(player2, List.of(new Terror()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInGraveyard(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Returning a marked creature to hand does not exile it")
    void markedCreatureCanReturnToHand() {
        Permanent avatar = harness.addToBattlefieldAndReturn(player2, new AvatarOfMight());
        harness.setHand(player1, List.of(new Scorchmark(), new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, avatar.getId());
        harness.castAndResolveInstant(player1, 0, avatar.getId());

        harness.assertNotOnBattlefield(player2, "Avatar of Might");
        harness.assertInHand(player2, "Avatar of Might");
        assertThat(gd.exiledCards).noneMatch(e -> e.card().getName().equals("Avatar of Might"));
    }

    @Test
    @DisplayName("Does not resolve when its only target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Scorchmark()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.castAndResolveInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Scorchmark");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Applies the exile replacement even when all damage is prevented")
    void appliesReplacementWhenDamageIsPrevented() {
        Permanent choManno = harness.addToBattlefieldAndReturn(player2, new ChoMannoRevolutionary());
        harness.setHand(player1, List.of(new Scorchmark(), new Terror()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, choManno.getId());
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        assertThat(choManno.getMarkedDamage()).isZero();
        harness.castAndResolveInstant(player1, 0, choManno.getId());

        harness.assertNotOnBattlefield(player2, "Cho-Manno, Revolutionary");
        harness.assertNotInGraveyard(player2, "Cho-Manno, Revolutionary");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Cho-Manno, Revolutionary"));
    }
}
