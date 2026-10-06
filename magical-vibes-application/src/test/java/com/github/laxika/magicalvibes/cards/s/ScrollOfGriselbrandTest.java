package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HarvesterOfSouls;
import com.github.laxika.magicalvibes.cards.j.JointAssault;
import com.github.laxika.magicalvibes.cards.t.TimberlandGuide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrollOfGriselbrand.class, Forest.class, TimberlandGuide.class, JointAssault.class,
        HarvesterOfSouls.class})
class ScrollOfGriselbrandTest extends BaseCardTest {

    private void setupScroll() {
        harness.addToBattlefield(player1, new ScrollOfGriselbrand());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, new ArrayList<>(List.of(new TimberlandGuide(), new Forest(), new JointAssault())));
    }

    @Test
    @DisplayName("Target opponent discards a card and the Scroll is sacrificed")
    void opponentDiscardsAndScrollSacrificed() {
        setupScroll();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Scroll of Griselbrand");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Opponent also loses 3 life when you control a Demon")
    void opponentLosesLifeWithDemon() {
        setupScroll();
        harness.addToBattlefield(player1, new HarvesterOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        setupScroll();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty-handed opponent still loses life when you control a Demon")
    void emptyHandDoesNotPreventLifeLoss() {
        setupScroll();
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new HarvesterOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Demon does not enable the life loss")
    void opponentsDemonDoesNotCount() {
        setupScroll();
        harness.addToBattlefield(player2, new HarvesterOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 2);

        harness.assertInGraveyard(player2, "Joint Assault");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A Demon acquired after activation enables life loss at resolution")
    void demonIsCheckedAtResolution() {
        setupScroll();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.addToBattlefield(player1, new HarvesterOfSouls());

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("A Demon removed before resolution does not enable life loss")
    void demonMustStillBeControlledAtResolution() {
        setupScroll();
        harness.addToBattlefield(player1, new HarvesterOfSouls());
        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Demons still cause only 3 life to be lost")
    void multipleDemonsDoNotMultiplyLifeLoss() {
        setupScroll();
        harness.addToBattlefield(player1, new HarvesterOfSouls());
        harness.addToBattlefield(player1, new HarvesterOfSouls());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
