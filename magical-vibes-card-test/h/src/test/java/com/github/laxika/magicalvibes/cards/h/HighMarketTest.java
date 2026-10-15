package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighMarket.class, FreshVolunteers.class})
class HighMarketTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new HighMarket());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping and sacrificing a creature gains 1 life")
    void sacrificeCreatureGainsOneLife() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new FreshVolunteers());

        int lifeBefore = gd.getLife(player1.getId());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability without a creature")
    void cannotSacrificeWithoutCreature() {
        harness.addToBattlefield(player1, new HighMarket());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentCreature() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player2, new FreshVolunteers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate the sacrifice ability when High Market is already tapped")
    void cannotSacrificeWhenTapped() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new FreshVolunteers());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("The creature is sacrificed as a cost before life is gained")
    void sacrificeIsPaidBeforeResolution() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new FreshVolunteers());
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Fresh Volunteers");
        harness.assertInGraveyard(player1, "Fresh Volunteers");
        harness.assertLife(player1, lifeBefore);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    @DisplayName("The controller chooses which creature to sacrifice, including a tapped creature")
    void choosesTappedCreatureAmongMultipleCreatures() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new FreshVolunteers());
        harness.addToBattlefield(player1, new FreshVolunteers());
        var survivor = gd.playerBattlefields.get(player1.getId()).get(1);
        var sacrificed = gd.playerBattlefields.get(player1.getId()).get(2);
        sacrificed.tap();
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(survivor).doesNotContain(sacrificed);
        harness.assertInGraveyard(player1, "Fresh Volunteers");
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("The activated ability resolves even after High Market leaves the battlefield")
    void resolvesWithoutSourceOnBattlefield() {
        harness.addToBattlefield(player1, new HighMarket());
        harness.addToBattlefield(player1, new FreshVolunteers());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        var market = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(market.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }
}
