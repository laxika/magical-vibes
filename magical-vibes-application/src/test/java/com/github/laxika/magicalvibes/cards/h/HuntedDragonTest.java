package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(HuntedDragon.class)
class HuntedDragonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates three first-strike Knight tokens under the targeted opponent's control")
    void etbCreatesKnightTokensForTargetOpponent() {
        harness.setHand(player1, List.of(new HuntedDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, player2.getId());
        resolveAllTriggers();

        List<Permanent> knights = findPermanents(player2, "Knight");
        assertThat(knights).hasSize(3);
        assertThat(findPermanents(player1, "Knight")).isEmpty();
        for (Permanent knight : knights) {
            assertThat(knight.getCard().isToken()).isTrue();
            assertThat(knight.getCard().getPower()).isEqualTo(2);
            assertThat(knight.getCard().getToughness()).isEqualTo(2);
            assertThat(knight.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(knight.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(knight.getCard().getSubtypes()).containsExactly(CardSubtype.KNIGHT);
            assertThat(gqs.hasKeyword(gd, knight, Keyword.FIRST_STRIKE)).isTrue();
        }
    }

    @Test
    @DisplayName("ETB target selection still only allows an opponent when it enters without being cast")
    void etbChoosesOpponentWhenEnteringWithoutBeingCast() {
        harness.enterBattlefieldAndReturn(player1, new HuntedDragon());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Knight")).hasSize(3);
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the controller with the ETB ability")
    void etbRequiresOpponentTarget() {
        harness.setHand(player1, List.of(new HuntedDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent targeting follows the Dragon's controller")
    void etbTargetsOpponentOfSecondPlayer() {
        harness.enterBattlefieldAndReturn(player2, new HuntedDragon());

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validIds()).containsExactly(player1.getId());

        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Knight")).hasSize(3);
        assertThat(findPermanents(player2, "Knight")).isEmpty();
    }

    @Test
    @DisplayName("The opponent still creates Knights after the Dragon leaves the battlefield")
    void etbResolvesAfterDragonLeavesBattlefield() {
        Permanent dragon = harness.enterBattlefieldAndReturn(player1, new HuntedDragon());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, dragon));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Hunted Dragon");
        harness.assertNotOnBattlefield(player1, "Hunted Dragon");
        assertThat(findPermanents(player2, "Knight")).hasSize(3);
        assertThat(findPermanents(player1, "Knight")).isEmpty();
    }
}
