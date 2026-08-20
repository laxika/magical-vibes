package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SurrakElusiveHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent spell targets a creature you control")
    void drawsWhenOpponentSpellTargetsCreature() {
        SurrakElusiveHunter surrak = new SurrakElusiveHunter();
        harness.addToBattlefield(player1, surrak);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, surrak.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(surrak.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Draws a card when an opponent targets a creature spell you control")
    void drawsWhenOpponentTargetsCreatureSpell() {
        SurrakElusiveHunter surrak = new SurrakElusiveHunter();
        harness.addToBattlefield(player1, surrak);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(surrak.getId());

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Draws a card when an opponent ability targets a creature you control")
    void drawsWhenOpponentAbilityTargetsCreature() {
        SurrakElusiveHunter surrak = new SurrakElusiveHunter();
        harness.addToBattlefield(player1, surrak);

        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player2, 0, null, surrak.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getId()).isEqualTo(surrak.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Does not trigger when your own spell targets your creature")
    void doesNotTriggerOnYourOwnSpell() {
        SurrakElusiveHunter surrak = new SurrakElusiveHunter();
        harness.addToBattlefield(player1, surrak);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, surrak.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot be countered")
    void cannotBeCountered() {
        SurrakElusiveHunter surrak = new SurrakElusiveHunter();
        harness.setHand(player1, List.of(surrak));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Counterspell()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, surrak.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(surrak.getId()));
        harness.assertInGraveyard(player2, "Counterspell");
    }
}
