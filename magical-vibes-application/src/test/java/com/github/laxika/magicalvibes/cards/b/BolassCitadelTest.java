package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BolassCitadel.class, Forest.class, GrizzlyBears.class})
class BolassCitadelTest extends BaseCardTest {

    @Test
    @DisplayName("casts a nonland spell from the top of the library by paying its mana value in life")
    void castsSpellFromLibraryTopByPayingLife() {
        harness.addToBattlefield(player1, new BolassCitadel());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);

        harness.castAndResolveFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("plays a land from the top of the library")
    void playsLandFromLibraryTop() {
        harness.addToBattlefield(player1, new BolassCitadel());
        Forest forest = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(forest);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromLibraryTop(player1);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("cannot cast the top spell when its mana value exceeds life")
    void cannotCastTopSpellWithoutEnoughLife() {
        harness.addToBattlefield(player1, new BolassCitadel());
        GrizzlyBears bears = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(bears);
        gd.playerLifeTotals.put(player1.getId(), 1);

        assertThatThrownBy(() -> harness.castFromLibraryTop(player1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("life");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(bears);
    }

    @Test
    @DisplayName("sacrifices ten nonland permanents and makes each opponent lose 10 life")
    void sacrificesTenNonlandPermanents() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player1, new BolassCitadel());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(10);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(10);
        assertThat(citadel.isTapped()).isTrue();
    }
}
