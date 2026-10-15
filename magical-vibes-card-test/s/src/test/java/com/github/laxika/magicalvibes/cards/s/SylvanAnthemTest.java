package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SylvanAnthem.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class SylvanAnthemTest extends BaseCardTest {

    @Test
    @DisplayName("Gives green creatures you control +1/+1")
    void buffsGreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff non-green creatures or creatures an opponent controls")
    void onlyBuffsGreenCreaturesYouControl() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, hillGiant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hillGiant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Scries 1 when a green creature enters under its controller's control")
    void scriesForGreenCreatureEntering() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }

    @Test
    @DisplayName("Does not scry when a non-green creature enters")
    void doesNotScryForNonGreenCreature() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's green creature")
    void doesNotScryForOpponentsGreenCreature() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Multiple Anthems stack their boosts and scry separately")
    void multipleAnthemsBoostAndScrySeparately() {
        harness.addToBattlefield(player1, new SylvanAnthem());
        harness.addToBattlefield(player1, new SylvanAnthem());
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).get(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(second);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("An animated Anthem boosts itself")
    void animatedAnthemBoostsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent anthem = harness.addToBattlefieldAndReturn(player1, new SylvanAnthem());

        assertThat(gqs.isCreature(gd, anthem)).isTrue();
        assertThat(gqs.getEffectivePower(gd, anthem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, anthem)).isEqualTo(3);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("An Anthem entering as a creature triggers its own scry ability")
    void animatedAnthemScriesForItsOwnEntry() {
        harness.addToBattlefield(player1, new Opalescence());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new SylvanAnthem(), "{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
    }
}
