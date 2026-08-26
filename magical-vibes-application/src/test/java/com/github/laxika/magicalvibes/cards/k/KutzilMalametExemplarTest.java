package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KutzilMalametExemplar.class, GrizzlyBears.class, Shock.class})
class KutzilMalametExemplarTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot cast spells during Kutzil's controller's turn")
    void opponentsCannotCastDuringControllerTurn() {
        Permanent kutzil = harness.addToBattlefieldAndReturn(player1, new KutzilMalametExemplar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, kutzil.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Combat damage from an unmodified creature does not draw")
    void unmodifiedCreatureDoesNotDraw() {
        addKutzil();
        addReadyAttacker();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Combat damage from a creature with greater power than base power draws")
    void modifiedCreatureDraws() {
        addKutzil();
        Permanent attacker = addReadyAttacker();
        attacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Multiple qualifying creatures cause only one draw")
    void multipleQualifyingCreaturesDrawOnce() {
        addKutzil();
        Permanent firstAttacker = addReadyAttacker();
        firstAttacker.setPowerModifier(1);
        Permanent secondAttacker = addReadyAttacker();
        secondAttacker.setPowerModifier(1);
        Card topCard = new GrizzlyBears();
        Card nextCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, nextCard));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard).doesNotContain(nextCard);
    }

    private Permanent addKutzil() {
        return harness.addToBattlefieldAndReturn(player1, new KutzilMalametExemplar());
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        return attacker;
    }
}
