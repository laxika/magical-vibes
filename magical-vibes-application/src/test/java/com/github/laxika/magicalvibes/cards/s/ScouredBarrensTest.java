package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScouredBarrens.class})
class ScouredBarrensTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new ScouredBarrens()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Scoured Barrens").isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(10);

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Tapping adds white mana")
    void addsWhiteMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping adds black mana")
    void addsBlackMana() {
        Permanent land = addReadyLand();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped land cannot activate its mana ability")
    void tappedLandCannotProduceMana() {
        harness.setHand(player1, List.of(new ScouredBarrens()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Life gain resolves even after the land leaves the battlefield")
    void gainsLifeAfterLandLeaves() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 15);
        harness.setHand(player1, List.of(new ScouredBarrens()));
        harness.playLand(player1, 0);
        Permanent land = findPermanent(player1, "Scoured Barrens");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Scoured Barrens");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("A newly controlled noncreature land produces only the chosen mana immediately")
    void newlyControlledLandProducesChosenManaWithoutUsingStack() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ScouredBarrens());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyLand() {
        return addCreatureReady(player1, new ScouredBarrens());
    }
}
