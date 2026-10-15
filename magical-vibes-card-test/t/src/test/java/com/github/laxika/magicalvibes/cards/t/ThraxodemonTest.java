package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Thraxodemon.class, GrizzlyBears.class, LeoninScimitar.class})
class ThraxodemonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and sacrificing another creature draws a card")
    void sacrificesCreatureAndDrawsCard() {
        Permanent demon = addReadyDemon();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Tapping and sacrificing another artifact draws a card")
    void sacrificesArtifactAndDrawsCard() {
        Permanent demon = addReadyDemon();
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(demon.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    @DisplayName("Cannot activate without another creature or artifact")
    void requiresAnotherCreatureOrArtifact() {
        addReadyDemon();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid on activation and exactly one card is drawn on resolution")
    void paysSacrificeBeforeDrawing() {
        Permanent demon = addReadyDemon();
        harness.addToBattlefield(player1, new Thraxodemon());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new LeoninScimitar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(demon.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(demon);
        harness.assertInGraveyard(player1, "Thraxodemon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's creature or artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsPermanents() {
        Permanent demon = addReadyDemon();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(demon.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new Thraxodemon());
        demon.setSummoningSick(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(demon.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent demon = addReadyDemon();
        demon.tap();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot activate with less than three mana")
    void requiresThreeMana() {
        Permanent demon = addReadyDemon();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(demon.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private Permanent addReadyDemon() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new Thraxodemon());
        demon.setSummoningSick(false);
        return demon;
    }
}
