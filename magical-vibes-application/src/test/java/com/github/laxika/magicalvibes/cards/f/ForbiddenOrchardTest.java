package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForbiddenOrchard.class})
class ForbiddenOrchardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds the chosen color and gives the opponent a Spirit token")
    void tapForManaGivesOpponentSpirit() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ForbiddenOrchard());
        int before = gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(before + 1);
        assertThat(land.isTapped()).isTrue();

        harness.passBothPriorities();

        Permanent spirit = findPermanents(player2, "Spirit").getFirst();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(spirit.getCard().getColors()).isEmpty();
        assertThat(spirit.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.getCard().isToken()).isTrue();
        harness.assertNotOnBattlefield(player1, "Spirit");
    }

    @Test
    @DisplayName("A second activation on a later turn makes a second Spirit for the opponent")
    void eachTapMakesAnotherSpirit() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.passBothPriorities();

        advanceToNextTurn();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Only the Orchard that was tapped creates the opponent's Spirit token")
    void onlyTappedOrchardTriggers() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("No Spirit is created while the land is untapped")
    void noSpiritWithoutTapping() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Mana is available immediately but the Spirit waits for the targeted trigger to resolve")
    void spiritCreationUsesTheStack() {
        harness.addToBattlefield(player1, new ForbiddenOrchard());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getLast().getTargetId()).isEqualTo(player2.getId());

        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Orchard gives its Spirit to the other player")
    void opponentOrchardGivesSpiritToUs() {
        harness.addToBattlefield(player2, new ForbiddenOrchard());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, "BLUE");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    private void advanceToNextTurn() {
        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
    }
}
