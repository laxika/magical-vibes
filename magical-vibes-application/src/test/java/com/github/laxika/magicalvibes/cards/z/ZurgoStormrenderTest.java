package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.c.CarrionFeeder;
import com.github.laxika.magicalvibes.cards.b.BlasphemousAct;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZurgoStormrender.class, CarrionFeeder.class,
        BlasphemousAct.class, SwordsToPlowshares.class})
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
        harness.setLibrary(player1, List.of(new ZurgoStormrender()));

        declareAttackers(List.of(0));
        resolveAllTriggers();
        int lifeAfterCombat = gd.getLife(player2.getId());

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeAfterCombat - 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attacking token leaving the battlefield draws a card")
    void attackingTokenDrawsACard() {
        addZurgoReady(player1);
        Permanent feeder = addCreatureReady(player1, new CarrionFeeder());
        harness.setLibrary(player1, List.of(new ZurgoStormrender()));

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
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Zurgo still triggers when it dies simultaneously with its nonattacking token")
    void simultaneousDeathsStillCauseLifeLoss() {
        addZurgoReady(player1);
        harness.setLibrary(player1, List.of(new ZurgoStormrender()));
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(TurnStep.POSTCOMBAT_MAIN);
        int opponentLife = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new BlasphemousAct()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife - 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("An attacking token being exiled draws a card without causing opponent life loss")
    void exilingAttackingTokenDrawsACard() {
        addZurgoReady(player1);
        harness.setLibrary(player1, List.of(new ZurgoStormrender()));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            Permanent token = findPermanent(player1, "Warrior");
            int opponentLife = gd.getLife(player2.getId());
            harness.setHand(player1, List.of(new SwordsToPlowshares()));
            harness.addMana(player1, ManaColor.WHITE, 1);

            harness.castAndResolveInstant(player1, 0, token.getId());
            resolveAllTriggers();

            assertThat(countPermanents(player1, "Warrior")).isZero();
            assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
            assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
            assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
        });
    }

    @Test
    @DisplayName("A nontoken creature leaving does not trigger Zurgo")
    void nontokenCreatureDoesNotTrigger() {
        addZurgoReady(player1);
        Permanent feeder = addCreatureReady(player1, new CarrionFeeder());
        Permanent sacrifice = addCreatureReady(player1, new CarrionFeeder());
        harness.setLibrary(player1, List.of(new ZurgoStormrender()));
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(feeder), null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Carrion Feeder");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
    }

    private Permanent addZurgoReady(Player player) {
        return addCreatureReady(player, new ZurgoStormrender());
    }
}
