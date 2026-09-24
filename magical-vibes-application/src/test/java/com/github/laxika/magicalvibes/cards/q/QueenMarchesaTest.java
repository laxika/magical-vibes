package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed(QueenMarchesa.class)
class QueenMarchesaTest extends BaseCardTest {

    @Test
    @DisplayName("Its controller becomes the monarch when it enters")
    void becomesMonarchOnEntry() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        resolveAllTriggers();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Creates a hasty deathtouch Assassin during upkeep when an opponent is the monarch")
    void createsAssassinWhenOpponentIsMonarch() {
        harness.addToBattlefield(player1, new QueenMarchesa());
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Assassin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent assassin = tokens.getFirst();
        assertThat(assassin.getCard().getPower()).isEqualTo(1);
        assertThat(assassin.getCard().getToughness()).isEqualTo(1);
        assertThat(assassin.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(assassin.getCard().hasKeyword(Keyword.DEATHTOUCH)).isTrue();
        assertThat(assassin.getCard().hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not create an Assassin when there is no monarch")
    void doesNotCreateAssassinWithoutMonarch() {
        harness.addToBattlefield(player1, new QueenMarchesa());

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not create an Assassin when its controller is the monarch")
    void doesNotCreateAssassinWhenControllerIsMonarch() {
        harness.addToBattlefield(player1, new QueenMarchesa());
        gd.monarchPlayerId = player1.getId();

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rechecks the opponent-monarch condition when the trigger resolves")
    void rechecksOpponentMonarchConditionAtResolution() {
        harness.addToBattlefield(player1, new QueenMarchesa());
        gd.monarchPlayerId = player2.getId();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        gd.monarchPlayerId = player1.getId();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
    }
    @Test
    void entersAndMakesItsControllerTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();

        assertThat(gd.monarchPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void createsAnAssassinDuringUpkeepWhenAnOpponentIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player2, new QueenMarchesa());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new QueenMarchesa());

        advanceToQueenUpkeep(player1);
        harness.passBothPriorities();

        Permanent assassin = findPermanent(player1, "Assassin");
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, assassin, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotCreateAnAssassinWhenItsControllerIsTheMonarch() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();

        advanceToQueenUpkeep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Assassin")).isEmpty();
    }

    @Test
    void combatDamageToTheMonarchTransfersTheMonarchDesignation() {
        harness.enterBattlefieldAndReturn(player1, new QueenMarchesa());
        harness.passBothPriorities();
        findPermanent(player1, "Queen Marchesa").tap();
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        int bearIndex = gd.playerBattlefields.get(player2.getId()).indexOf(bear);
        declareAttackers(player2, List.of(bearIndex));
        resolveCombat(player2);

        assertThat(gd.monarchPlayerId).isEqualTo(player2.getId());
    }

    private void advanceToQueenUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.UPKEEP);
    }

}
