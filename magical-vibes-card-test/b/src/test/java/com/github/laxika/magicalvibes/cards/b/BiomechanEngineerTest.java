package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BiomechanEngineer.class, Forest.class, Island.class})
class BiomechanEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates a Lander token")
    void enteringCreatesLander() {
        harness.setHand(player1, List.of(new BiomechanEngineer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
    }

    @Test
    @DisplayName("The activated ability draws two cards and creates a Robot")
    void activatedAbilityDrawsAndCreatesRobot() {
        Card firstDraw = new Forest();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new BiomechanEngineer());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(engineer), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(firstDraw, secondDraw);
        Permanent robot = findPermanent(player1, "Robot");
        assertThat(robot.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(robot.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(robot.getCard().getPower()).isEqualTo(2);
        assertThat(robot.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    void landerSacrificesImmediatelyAndFindsABasicLandTapped() {
        Forest forest = new Forest();
        BiomechanEngineer nonland = new BiomechanEngineer();
        harness.setLibrary(player1, List.of(forest, nonland));
        harness.enterBattlefieldAndReturn(player1, new BiomechanEngineer());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), 0, null, null);

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(lander.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(findPermanents(player2, "Forest")).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landerCanResolveWithoutAnyBasicLandInLibrary() {
        BiomechanEngineer nonland = new BiomechanEngineer();
        harness.setLibrary(player1, List.of(nonland));
        harness.enterBattlefieldAndReturn(player1, new BiomechanEngineer());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void landerMayFailToFindEvenWithABasicLandAvailable() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.enterBattlefieldAndReturn(player1, new BiomechanEngineer());
        resolveAllTriggers();
        Permanent lander = findPermanent(player1, "Lander");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(lander), 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(findPermanents(player1, "Lander")).isEmpty();
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void tappedEngineerCanActivateTwiceWithoutWaitingATurn() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        Permanent engineer = harness.addToBattlefieldAndReturn(player1, new BiomechanEngineer());
        engineer.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(findPermanents(player1, "Robot")).hasSize(2);
        assertThat(findPermanents(player1, "Robot")).allSatisfy(robot -> {
            assertThat(robot.getCard().getColors()).isEmpty();
            assertThat(robot.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
            assertThat(robot.isTapped()).isFalse();
        });
        assertThat(engineer.isTapped()).isTrue();
    }
}
