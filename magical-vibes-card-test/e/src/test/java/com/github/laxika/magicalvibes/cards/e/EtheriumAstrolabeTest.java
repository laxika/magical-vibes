package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CylianElf;
import com.github.laxika.magicalvibes.cards.o.ObeliskOfEsper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EtheriumAstrolabe.class, ObeliskOfEsper.class, CylianElf.class})
class EtheriumAstrolabeTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {B}, tapping, and sacrificing an artifact draws a card")
    void drawsCardOnResolution() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        java.util.UUID artifactId = harness.getPermanentId(player1, "Obelisk of Esper");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Obelisk of Esper");
    }

    @Test
    @DisplayName("Can sacrifice itself to pay the ability and still draws")
    void canSacrificeItselfToDraw() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Etherium Astrolabe");
    }

    @Test
    @DisplayName("Cannot activate the ability without paying the {B} mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        harness.setLibrary(player1, List.of(new CylianElf()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating taps Etherium Astrolabe when sacrificing another artifact")
    void tapsOnActivation() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        java.util.UUID artifactId = harness.getPermanentId(player1, "Obelisk of Esper");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifactId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Etherium Astrolabe").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        harness.addToBattlefield(player1, new ObeliskOfEsper());
        harness.addMana(player1, ManaColor.BLACK, 1);
        findPermanent(player1, "Etherium Astrolabe").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature artifact can activate while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent astrolabe = harness.addToBattlefieldAndReturn(player1, new EtheriumAstrolabe());
        astrolabe.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Etherium Astrolabe");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EtheriumAstrolabe()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Etherium Astrolabe");
    }

    @Test
    @DisplayName("Sacrifice is paid before resolution, even with a tapped artifact")
    void sacrificesTappedArtifactBeforeDrawing() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfEsper());
        artifact.tap();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new CylianElf(), new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Obelisk of Esper");
        harness.assertInGraveyard(player1, "Obelisk of Esper");
        assertThat(findPermanent(player1, "Etherium Astrolabe").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A nonartifact creature cannot be sacrificed to pay the cost")
    void cannotSacrificeNonartifact() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfEsper());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CylianElf());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Cylian Elf");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");
    }

    @Test
    @DisplayName("An opponent's artifact cannot be sacrificed to pay the cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new EtheriumAstrolabe());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new ObeliskOfEsper());
        Permanent opponentsArtifact = harness.addToBattlefieldAndReturn(player2, new ObeliskOfEsper());
        harness.setLibrary(player1, List.of(new CylianElf()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentsArtifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Obelisk of Esper");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Cylian Elf");
    }
}
