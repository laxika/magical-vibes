package com.github.laxika.magicalvibes.cards.a;

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

@CardUsed({Ahriman.class, GrizzlyBears.class, LeoninScimitar.class})
class AhrimanTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws a card")
    void sacrificesAnotherCreatureAndDraws() {
        Permanent ahriman = addAhrimanReady();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeoninScimitar());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ahriman);
    }

    @Test
    @DisplayName("Sacrificing an artifact draws a card")
    void sacrificesArtifactAndDraws() {
        Permanent ahriman = addAhrimanReady();
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new GrizzlyBears());
        LeoninScimitar drawn = new LeoninScimitar();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, scimitar.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ahriman);
    }

    @Test
    @DisplayName("Cannot sacrifice Ahriman itself")
    void requiresAnotherCreatureOrArtifact() {
        addAhrimanReady();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifice is paid before the card is drawn")
    void paysSacrificeBeforeResolution() {
        addAhrimanReady();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ahriman());
        Ahriman drawn = new Ahriman();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Ahriman");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Ahriman can activate its ability")
    void activatesWhileTappedAndSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Ahriman());
        source.setSummoningSick(true);
        source.setTapped(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ahriman());
        Ahriman drawn = new Ahriman();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(source);
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        addAhrimanReady();
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Ahriman());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentCreature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two mana is insufficient to activate the ability")
    void requiresThreeMana() {
        addAhrimanReady();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new Ahriman());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addAhrimanReady() {
        return addCreatureReady(player1, new Ahriman());
    }
}
