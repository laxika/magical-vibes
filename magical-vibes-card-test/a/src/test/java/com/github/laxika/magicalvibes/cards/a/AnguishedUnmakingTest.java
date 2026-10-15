package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MagnifyingGlass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnguishedUnmaking.class, Forest.class, GrizzlyBears.class, MagnifyingGlass.class})
class AnguishedUnmakingTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles target nonland permanent and the spell controller loses 3 life")
    void exilesTargetNonlandPermanentAndLosesLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        int controllerLifeBefore = gd.getLife(player1.getId());
        int targetControllerLifeBefore = gd.getLife(player2.getId());

        castAnguishedUnmaking(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore - 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(targetControllerLifeBefore);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new Forest()).getId();

        harness.setHand(player1, List.of(new AnguishedUnmaking()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Does not lose life when the target leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        int lifeBefore = gd.getLife(player1.getId());

        castAnguishedUnmaking(targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile a permanent controlled by the caster")
    void canExileOwnPermanent() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        int lifeBefore = gd.getLife(player1.getId());

        castAnguishedUnmaking(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, lifeBefore - 3);
    }

    @Test
    @DisplayName("Life loss is an effect, so the spell can be cast with less than 3 life")
    void canCastWithLessThanThreeLife() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setLife(player1, 2);

        castAnguishedUnmaking(targetId);
        harness.assertLife(player1, 2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        harness.assertLife(player1, -1);
    }
    @Test
    @DisplayName("Exiles a noncreature artifact and loses 3 life")
    void exilesNoncreatureArtifact() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MagnifyingGlass()).getId();
        int lifeBefore = gd.getLife(player1.getId());

        castAnguishedUnmaking(targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Magnifying Glass");
        harness.assertNotInGraveyard(player2, "Magnifying Glass");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Magnifying Glass"));
        harness.assertLife(player1, lifeBefore - 3);
    }
    private void castAnguishedUnmaking(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new AnguishedUnmaking()));
        addMana();
        harness.castInstant(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
