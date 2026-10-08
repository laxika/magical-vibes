package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.t.TajuruStalwart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VampiricRites.class, TajuruStalwart.class})
class VampiricRitesTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gains 1 life and draws a card")
    void sacrificesCreatureGainsLifeAndDrawsCard() {
        harness.addToBattlefield(player1, new VampiricRites());
        harness.addToBattlefield(player1, new TajuruStalwart());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VampiricRites()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        harness.assertInHand(player1, "Vampiric Rites");
        harness.assertInGraveyard(player1, "Tajuru Stalwart");
    }

    @Test
    @DisplayName("Cannot be activated without a creature to sacrifice")
    void requiresCreatureToSacrifice() {
        harness.addToBattlefield(player1, new VampiricRites());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new VampiricRites());
        harness.addToBattlefield(player2, new TajuruStalwart());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Tajuru Stalwart");
        harness.assertNotInGraveyard(player2, "Tajuru Stalwart");
    }

    @Test
    @DisplayName("The activation requires black mana in addition to generic mana")
    void requiresBlackMana() {
        harness.addToBattlefield(player1, new VampiricRites());
        harness.addToBattlefield(player1, new TajuruStalwart());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Tajuru Stalwart");
        harness.assertNotInGraveyard(player1, "Tajuru Stalwart");
    }

    @Test
    @DisplayName("With multiple creatures, only the chosen creature is sacrificed")
    void sacrificesOnlyChosenCreature() {
        harness.addToBattlefield(player1, new VampiricRites());
        var first = harness.addToBattlefieldAndReturn(player1, new TajuruStalwart());
        var second = harness.addToBattlefieldAndReturn(player1, new TajuruStalwart());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new VampiricRites(), new VampiricRites()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        harness.assertInGraveyard(player1, "Tajuru Stalwart");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Vampiric Rites");
    }
}
