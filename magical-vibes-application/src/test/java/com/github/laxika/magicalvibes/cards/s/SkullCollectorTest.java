package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HandOfCruelty;
import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkullCollector.class, HandOfCruelty.class, HandOfHonor.class})
class SkullCollectorTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep only offers black creatures the controller controls")
    void upkeepOnlyOffersControlledBlackCreatures() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, new HandOfCruelty());
        Permanent nonblackCreature = harness.addToBattlefieldAndReturn(player1, new HandOfHonor());
        Permanent opponentBlackCreature = harness.addToBattlefieldAndReturn(player2, new HandOfCruelty());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.PermanentChoice choice =
                gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(collector.getId(), blackCreature.getId())
                .doesNotContain(nonblackCreature.getId(), opponentBlackCreature.getId());
    }

    @Test
    @DisplayName("The chosen black creature returns to its owner's hand")
    void chosenBlackCreatureReturnsToHand() {
        harness.addToBattlefield(player1, new SkullCollector());
        Card blackCreatureCard = new HandOfCruelty();
        blackCreatureCard.setOwnerId(player2.getId());
        Permanent blackCreature = harness.addToBattlefieldAndReturn(player1, blackCreatureCard);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, blackCreature.getId());

        harness.assertNotOnBattlefield(player1, "Hand of Cruelty");
        harness.assertNotInHand(player1, "Hand of Cruelty");
        harness.assertInHand(player2, "Hand of Cruelty");
        harness.assertOnBattlefield(player1, "Skull Collector");
    }

    @Test
    @DisplayName("The upkeep trigger returns Skull Collector when it is the only black creature")
    void returnsItselfWhenOnlyBlackCreature() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        harness.addToBattlefield(player1, new HandOfHonor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, collector.getId());

        harness.assertNotOnBattlefield(player1, "Skull Collector");
        harness.assertInHand(player1, "Skull Collector");
    }

    @Test
    @DisplayName("The activated ability grants a regeneration shield")
    void activatedAbilityGrantsRegenerationShield() {
        Permanent collector = harness.addToBattlefieldAndReturn(player1, new SkullCollector());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(collector.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves Skull Collector from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent collector = addCreatureReady(player1, new SkullCollector());
        addCreatureReady(player2, new HandOfCruelty());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passUntil(player2, TurnStep.END_OF_COMBAT);

        harness.assertOnBattlefield(player1, "Skull Collector");
        assertThat(collector.isTapped()).isTrue();
        assertThat(collector.getMarkedDamage()).isZero();
        assertThat(collector.getRegenerationShield()).isZero();
    }
}
