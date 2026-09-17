package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HomewardPath.class, GrizzlyBears.class})
class HomewardPathTest extends BaseCardTest {

    @Test
    @DisplayName("Mana ability taps for {C}")
    void manaAbilityAddsColorless() {
        harness.addToBattlefield(player1, new HomewardPath());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each player regains control of creatures they own")
    void returnsOwnedCreaturesToTheirOwners() {
        harness.addToBattlefield(player1, new HomewardPath());

        Permanent playerOneCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.stolenCreatures.put(playerOneCreature.getId(), player1.getId());
        Permanent playerTwoCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(playerTwoCreature.getId(), player2.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(playerOneCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(playerTwoCreature);
    }
}
