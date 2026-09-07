package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SceneOfTheCrime.class, Forest.class, GrizzlyBears.class})
class SceneOfTheCrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SceneOfTheCrime()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Scene of the Crime").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps an untapped creature to add any-color mana")
    void tapsCreatureForAnyColorMana() {
        Permanent land = addReadyLand();
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(land.isTapped()).isTrue();
        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot add any-color mana without an untapped creature")
    void cannotTapForAnyColorManaWithoutCreature() {
        addReadyLand();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Sacrifices to draw a card")
    void sacrificesToDrawCard() {
        addReadyLand();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotOnBattlefield(player1, "Scene of the Crime");
        harness.assertInGraveyard(player1, "Scene of the Crime");
    }

    private Permanent addReadyLand() {
        Permanent land = new Permanent(new SceneOfTheCrime());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(land);
        return land;
    }
}
