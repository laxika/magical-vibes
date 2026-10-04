package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.s.SuddenDeath;
import com.github.laxika.magicalvibes.cards.r.Reiterate;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Evangelize.class, BenalishCavalry.class, SuddenDeath.class, Reiterate.class})
class EvangelizeTest extends BaseCardTest {

    private void addEvangelizeMana() {
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    @Test
    @DisplayName("The caster chooses an opponent, then that opponent chooses the creature")
    void opponentChoosesCreatureTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Permanent otherOpponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorcery(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice opponentChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(opponentChoice.playerId()).isEqualTo(player1.getId());
        assertThat(opponentChoice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.PermanentChoice creatureChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(creatureChoice.playerId()).isEqualTo(player2.getId());
        assertThat(creatureChoice.validIds()).containsExactly(
                opponentCreature.getId(), otherOpponentCreature.getId());

        harness.handlePermanentChosen(player2, opponentCreature.getId());

        assertThat(gd.stack).singleElement().satisfies(entry ->
                assertThat(entry.getOpponentChosenTargetPlayerId()).isEqualTo(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opponentCreature)
                .contains(otherOpponentCreature);
        harness.assertInGraveyard(player1, "Evangelize");
    }

    @Test
    @DisplayName("Evangelize fizzles if the chosen creature leaves before resolution")
    void chosenCreatureLeavingBeforeResolutionFizzles() {
        Permanent opponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
        harness.assertInGraveyard(player1, "Evangelize");
    }

    @Test
    @DisplayName("Buyback returns Evangelize after it resolves")
    void buybackReturnsToHand() {
        Permanent opponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        assertThat(gd.stack).singleElement().extracting(StackEntry::isBuyback).isEqualTo(true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).singleElement()
                .isInstanceOf(Evangelize.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Buyback does not return Evangelize when its only target becomes illegal")
    void buybackDoesNotReturnWhenTargetLeaves() {
        Permanent opponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        harness.setHand(player1, List.of(new Evangelize()));
        addEvangelizeMana();

        harness.castSorceryWithBuyback(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        harness.setHand(player2, List.of(new SuddenDeath()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Evangelize");
        harness.assertNotInHand(player1, "Evangelize");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(opponentCreature);
    }

    @Test
    @DisplayName("A copy cannot resolve after its target leaves the chosen opponent's control")
    void copyRetainsChosenOpponentRestriction() {
        Permanent opponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Evangelize evangelize = new Evangelize();
        harness.setHand(player1, List.of(evangelize, new Reiterate()));
        harness.setHand(player2, List.of(new Reiterate()));
        addEvangelizeMana();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());

        harness.castAndResolveInstant(player2, 0, evangelize.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.castAndResolveInstant(player1, 0, evangelize.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);

        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Evangelize");
    }

    @Test
    @DisplayName("New targets for a copy must remain under the chosen opponent's control")
    void copyNewTargetsExcludeCastersCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new BenalishCavalry());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Permanent otherOpponentCreature =
                harness.addToBattlefieldAndReturn(player2, new BenalishCavalry());
        Evangelize evangelize = new Evangelize();
        harness.setHand(player1, List.of(evangelize, new Reiterate()));
        addEvangelizeMana();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, opponentCreature.getId());
        harness.castAndResolveInstant(player1, 0, evangelize.getId());
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.playerId()).isEqualTo(player1.getId());
        assertThat(targetChoice.validIds())
                .contains(opponentCreature.getId(), otherOpponentCreature.getId())
                .doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, otherOpponentCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(ownCreature, opponentCreature, otherOpponentCreature);
    }
}
