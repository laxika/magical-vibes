package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OscorpResearchTeam.class})
class OscorpResearchTeamTest extends BaseCardTest {

    @Test
    @DisplayName("Paying six generic and one blue mana draws two cards")
    void payingAbilityCostDrawsTwoCards() {
        harness.addToBattlefield(player1, new OscorpResearchTeam());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new OscorpResearchTeam(), new OscorpResearchTeam()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Cannot activate without the full mana cost")
    void cannotActivateWithoutFullManaCost() {
        harness.addToBattlefield(player1, new OscorpResearchTeam());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick team can activate twice before either ability resolves")
    void canActivateTwiceWhileTappedAndSummoningSick() {
        var team = harness.addToBattlefieldAndReturn(player1, new OscorpResearchTeam());
        team.tap();
        team.setSummoningSick(true);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new OscorpResearchTeam(), new OscorpResearchTeam(),
                new OscorpResearchTeam(), new OscorpResearchTeam()));
        harness.addMana(player1, ManaColor.COLORLESS, 12);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(team.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activating during the opponent's turn draws only for the ability's controller")
    void drawsForControllerDuringOpponentsTurn() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player2, new OscorpResearchTeam());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new OscorpResearchTeam(), new OscorpResearchTeam()));
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
