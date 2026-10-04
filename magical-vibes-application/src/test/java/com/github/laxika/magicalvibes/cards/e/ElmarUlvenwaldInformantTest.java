package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElmarUlvenwaldInformant.class, GrizzlyBears.class, Forest.class, LightningBolt.class})
class ElmarUlvenwaldInformantTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell untaps a target creature and creates a Clue")
    void secondSpellUntapsCreatureAndInvestigates() {
        harness.addToBattlefield(player1, new ElmarUlvenwaldInformant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(target.getId());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("The first spell does not trigger Elmar")
    void firstSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ElmarUlvenwaldInformant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(target.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The triggered ability can target only creatures")
    void triggerCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new ElmarUlvenwaldInformant());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(creature.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();
    }

    @Test
    void untappedSelfIsLegalAndThirdSpellDoesNotTriggerAgain() {
        Permanent elmar = harness.addToBattlefieldAndReturn(player1, new ElmarUlvenwaldInformant());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, elmar.getId());
        harness.passBothPriorities();

        assertThat(elmar.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);

        harness.passBothPriorities();
        elmar.tap();
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(elmar.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void spellsCastBeforeElmarEnteredStillCount() {
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        Permanent elmar = harness.addToBattlefieldAndReturn(player1, new ElmarUlvenwaldInformant());
        elmar.tap();

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, elmar.getId());
        harness.passBothPriorities();

        assertThat(elmar.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void opponentsSecondSpellDoesNotTrigger() {
        Permanent elmar = harness.addToBattlefieldAndReturn(player1, new ElmarUlvenwaldInformant());
        elmar.tap();
        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(elmar.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void removingTargetInResponsePreventsInvestigation() {
        harness.addToBattlefield(player1, new ElmarUlvenwaldInformant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void secondSpellOnOpponentsTurnTriggersAfterCountResets() {
        Permanent elmar = harness.addToBattlefieldAndReturn(player1, new ElmarUlvenwaldInformant());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        elmar.tap();
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(elmar.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, elmar.getId());
        harness.passBothPriorities();

        assertThat(elmar.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void investigatedClueCanBeSacrificedForTwoManaToDraw() {
        Permanent elmar = harness.addToBattlefieldAndReturn(player1, new ElmarUlvenwaldInformant());
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt()));
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, elmar.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent clue = findPermanent(player1, "Clue");
        clue.tap();
        harness.addMana(player1, ManaColor.GREEN, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId()).indexOf(clue);
        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
