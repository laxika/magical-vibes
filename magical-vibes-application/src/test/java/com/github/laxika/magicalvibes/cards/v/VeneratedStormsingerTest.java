package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeneratedStormsinger.class, GrizzlyBears.class, LightningStrike.class})
class VeneratedStormsingerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking red Warrior token")
    void attackingCreatesTappedAndAttackingWarriorToken() {
        addCreatureReady(player1, new VeneratedStormsinger());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().isTapped()).isTrue();
        assertThat(tokens.getFirst().isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The mobilized token is sacrificed at the beginning of the next end step")
    void mobilizedTokenIsSacrificedAtNextEndStep() {
        addCreatureReady(player1, new VeneratedStormsinger());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isOne();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList()).isEmpty();
    }

    @Test
    @DisplayName("Another creature you control dying drains each opponent")
    void anotherCreatureYouControlDyingDrainsEachOpponent() {
        harness.addToBattlefield(player1, new VeneratedStormsinger());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killPermanent(player1, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The ability triggers when Venerated Stormsinger dies")
    void thisCreatureDyingDrainsEachOpponent() {
        harness.addToBattlefield(player1, new VeneratedStormsinger());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killPermanent(player1, "Venerated Stormsinger");

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger the ability")
    void opponentCreatureDyingDoesNotTrigger() {
        harness.addToBattlefield(player1, new VeneratedStormsinger());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        killPermanent(player2, "Grizzly Bears");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killPermanent(com.github.laxika.magicalvibes.model.Player controller, String name) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        UUID permanentId = harness.getPermanentId(controller, name);
        harness.castInstant(player2, 0, permanentId);
        harness.passBothPriorities();
    }
}
