package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RedGhostIntangibleGenius.class, Shock.class})
class RedGhostIntangibleGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Drawing the second card each turn creates a hasty red Ape Villain token")
    void secondDrawCreatesHastyApeToken() {
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());
        harness.setLibrary(player1, List.of(new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius()));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Ape")
                        && permanent.getCard().hasType(CardType.CREATURE)
                        && permanent.getCard().getColor() == CardColor.RED
                        && permanent.getCard().getPower() == 3
                        && permanent.getCard().getToughness() == 3
                        && permanent.getCard().getSubtypes().contains(CardSubtype.APE)
                        && permanent.getCard().getSubtypes().contains(CardSubtype.VILLAIN)
                        && permanent.getCard().getKeywords().contains(Keyword.HASTE));

        drawCard(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Red Ghost can't be blocked")
    void cannotBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new RedGhostIntangibleGenius());
        Permanent ghost = addCreatureReady(player1, new RedGhostIntangibleGenius());
        ghost.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(ghost);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPay() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new RedGhostIntangibleGenius());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, ghost.getId());
        resolveAllTriggers();

        assertThat(ghost.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void payingTwoManaForWardAllowsSpellToResolve() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new RedGhostIntangibleGenius());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, ghost.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(ghost.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent ghost = harness.addToBattlefieldAndReturn(player1, new RedGhostIntangibleGenius());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, ghost.getId());

        assertThat(ghost.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsSecondDrawDoesNotCreateToken() {
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());
        harness.setLibrary(player2, List.of(new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius()));

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void secondDrawOnOpponentsTurnCreatesToken() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());
        harness.setLibrary(player1, List.of(new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius()));

        drawCard(player1);
        drawCard(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ape")).isEqualTo(1);
    }

    @Test
    void firstDrawBeforeEnteringStillCounts() {
        harness.setLibrary(player1, List.of(new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius()));
        drawCard(player1);
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());

        drawCard(player1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Ape")).isEqualTo(1);
    }

    @Test
    void enteringAfterSecondDrawDoesNotTriggerOnThirdDraw() {
        harness.setLibrary(player1, List.of(new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius(), new RedGhostIntangibleGenius()));
        drawCard(player1);
        drawCard(player1);
        harness.addToBattlefield(player1, new RedGhostIntangibleGenius());

        drawCard(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    private void drawCard(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
