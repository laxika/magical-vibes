package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrganicExtinction.class, GrizzlyBears.class, HowlingMine.class, Ornithopter.class})
class OrganicExtinctionTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys nonartifact creatures and leaves artifact and noncreature permanents")
    void destroysNonartifactCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new HowlingMine());

        castOrganicExtinction();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Howling Mine");
    }

    @Test
    @DisplayName("Leaves artifact creatures on the battlefield")
    void leavesArtifactCreatures() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castOrganicExtinction();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Improvise taps both noncreature artifacts and newly entered artifact creatures")
    void improvisePaysGenericMana() {
        var mine = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        var thopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        thopter.setSummoningSick(true);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(mine.getId(), thopter.getId()));

        assertThat(mine.isTapped()).isTrue();
        assertThat(thopter.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Howling Mine");
        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Organic Extinction");
    }

    @Test
    @DisplayName("Improvise cannot replace required white mana")
    void improviseCannotPayColoredMana() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(artifact.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Nonartifact creatures cannot pay for improvise")
    void improviseRejectsNonartifactCreatures() {
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bear.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Already tapped artifacts cannot pay for improvise")
    void improviseRejectsTappedArtifacts() {
        var artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstantWithConvoke(player1, 0,
                List.of(), List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves without any creatures on the battlefield")
    void resolvesOnEmptyBattlefield() {
        castOrganicExtinction();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Organic Extinction");
    }

    private void castOrganicExtinction() {
        harness.setHand(player1, List.of(new OrganicExtinction()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
