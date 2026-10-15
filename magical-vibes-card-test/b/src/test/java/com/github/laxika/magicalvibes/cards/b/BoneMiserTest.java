package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.z.ZombieInfestation;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneMiser.class, ZombieInfestation.class, GrizzlyBears.class, Mountain.class, Spellbook.class})
class BoneMiserTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a creature creates a Zombie and discarding a land adds two black mana")
    void creatureAndLandDiscardTriggers() {
        addBoneMiserAndDiscardOutlet();
        harness.setHand(player1, List.of(new GrizzlyBears(), new Mountain()));

        discardTwoCards();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.ZOMBIE)))
                .hasSize(2);
    }

    @Test
    @DisplayName("Discarding noncreature nonland cards draws a card for each discard")
    void noncreatureNonlandDiscardTriggersDraws() {
        addBoneMiserAndDiscardOutlet();
        harness.setHand(player1, List.of(new Spellbook(), new Spellbook()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        discardTwoCards();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(2)
                .allMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Each discarded land produces mana only when its trigger resolves")
    void landDiscardTriggersUseTheStack() {
        addBoneMiserAndDiscardOutlet();
        harness.setHand(player1, List.of(new Mountain(), new Mountain()));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(countPermanents(player1, "Zombie")).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(countPermanents(player1, "Zombie")).isZero();

        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(countPermanents(player1, "Zombie")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each Bone Miser triggers independently for each discarded creature")
    void multipleCopiesTriggerForEachCreatureDiscarded() {
        addBoneMiserAndDiscardOutlet();
        harness.addToBattlefield(player1, new BoneMiser());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        discardTwoCards();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An opponent's discards do not trigger Bone Miser")
    void opponentDiscardDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BoneMiser());
        harness.addToBattlefield(player2, new ZombieInfestation());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Mountain()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void addBoneMiserAndDiscardOutlet() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new BoneMiser());
        harness.addToBattlefield(player1, new ZombieInfestation());
    }

    private void discardTwoCards() {
        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
