package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoStormrender.class, CarrionFeeder.class, GrizzlyBears.class})
class ZurgoStormrenderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a tapped and attacking Warrior token")
    void attackingCreatesMobilizedToken() {
        addZurgoReady(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        Permanent token = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("A nonattacking token leaving at the next end step makes each opponent lose 1 life")
    void nonattackingTokenMakesOpponentsLoseLife() {
        addZurgoReady(player1);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        int lifeAfterCombat = gd.getLife(player2.getId());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterCombat - 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attacking token leaving the battlefield draws a card")
    void attackingTokenDrawsACard() {
        addZurgoReady(player1);
        Permanent feeder = addCreatureReady(player1, new CarrionFeeder());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        Permanent token = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(feeder), null, null);
        harness.handlePermanentChosen(player1, token.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private Permanent addZurgoReady(Player player) {
        return addCreatureReady(player, new ZurgoStormrender());
    }
}
