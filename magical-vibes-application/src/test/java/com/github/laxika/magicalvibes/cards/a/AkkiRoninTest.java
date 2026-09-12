package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MothriderSamurai;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AkkiRonin.class, MothriderSamurai.class, ElvishWarrior.class, GrizzlyBears.class, Forest.class})
class AkkiRoninTest extends BaseCardTest {

    @Test
    @DisplayName("A Samurai attacking alone may discard and draw")
    void samuraiAttackingAloneMayDiscardAndDraw() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("A Warrior attacking alone may discard and draw")
    void warriorAttackingAloneMayDiscardAndDraw() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new ElvishWarrior());
        GrizzlyBears discarded = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining the attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A non-Samurai, non-Warrior attacking alone does not trigger")
    void otherCreatureAttackingAloneDoesNotTrigger() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("A Samurai attacking with another creature does not trigger")
    void samuraiAttackingWithAnotherCreatureDoesNotTrigger() {
        addCreatureReady(player1, new AkkiRonin());
        addCreatureReady(player1, new MothriderSamurai());
        addCreatureReady(player1, new GrizzlyBears());
        GrizzlyBears retained = new GrizzlyBears();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }
}
