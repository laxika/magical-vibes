package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    private Permanent addReadyDemon() {
        Permanent demon = harness.addToBattlefieldAndReturn(player1, new Thraxodemon());
        demon.setSummoningSick(false);
        return demon;
    }
}
