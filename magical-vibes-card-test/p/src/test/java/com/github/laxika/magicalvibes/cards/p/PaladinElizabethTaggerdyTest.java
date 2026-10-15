package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaladinElizabethTaggerdy.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class PaladinElizabethTaggerdyTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion draws a card and offers a creature within its power tapped and attacking")
    void battalionDrawsAndPutsCreatureTappedAndAttacking() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        HillGiant tooExpensive = new HillGiant();
        GrizzlyBears valid = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(tooExpensive, valid));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandChoice choice =
                (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        Permanent entered = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensive);
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithOnlyOneOtherAttacker() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    @DisplayName("The creature drawn by battalion can be put onto the battlefield")
    void canPutTheDrawnCreatureOntoTheBattlefield() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanents(player1, "Grizzly Bears").getLast();
        assertThat(entered.getCard().getId()).isEqualTo(drawn.getId());
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the optional creature does not undo the draw")
    void mayDeclinePuttingCreatureOntoTheBattlefield() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears creature = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, drawn);
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Battalion uses Taggerdy's power at resolution, including the exact limit")
    void usesPowerAtResolution() {
        Permanent taggerdy = addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0, 1, 2));
        taggerdy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Hill Giant");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Three other attackers do not trigger battalion when Taggerdy stays behind")
    void taggerdyMustAttack() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(1, 2, 3));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }

    @Test
    @DisplayName("Battalion uses last known power after Taggerdy leaves the battlefield")
    void usesLastKnownPowerAfterSourceLeaves() {
        Permanent taggerdy = addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0, 1, 2));
        taggerdy.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.getPermanentRemovalService().removePermanentToHand(gd, taggerdy);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.HandChoice choice =
                (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).contains(0);
        harness.handleCardChosen(player1, 0);

        Permanent entered = findPermanent(player1, "Hill Giant");
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttacking()).isTrue();
        harness.assertNotOnBattlefield(player1, "Paladin Elizabeth Taggerdy");
    }

    @Test
    @DisplayName("Battalion still resolves after another attacker leaves the battlefield")
    void battalionDoesNotRecheckAttackerCount() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0, 1, 2));
        harness.getPermanentRemovalService().removePermanentToHand(gd, other);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
