package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LoxodonSurveyor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KickoffCelebrations.class, Forest.class, LoxodonSurveyor.class, LumberingWorldwagon.class})
class KickoffCelebrationsTest extends BaseCardTest {

    @Test
    void enteringMayDiscardsOneAndDrawsTwo() {
        Card drawnOne = new Forest();
        Card drawnTwo = new Forest();
        Card discarded = new LoxodonSurveyor();
        harness.setLibrary(player1, List.of(drawnOne, drawnTwo));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new KickoffCelebrations(), discarded));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(drawnOne, drawnTwo);
    }

    @Test
    void maxSpeedAbilitySacrificesItAndGivesHasteToCreaturesAndVehicles() {
        Permanent kickoff = harness.addToBattlefieldAndReturn(player1, new KickoffCelebrations());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LoxodonSurveyor());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new LumberingWorldwagon());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(kickoff.getCard());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isFalse();
        assertThat(kickoff).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void maxSpeedAbilityCannotBeActivatedBelowMaxSpeed() {
        Permanent kickoff = harness.addToBattlefieldAndReturn(player1, new KickoffCelebrations());
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(kickoff).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void decliningDiscardDoesNotDrawAndStartsEngines() {
        Card kept = new LoxodonSurveyor();
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new KickoffCelebrations(), kept));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void emptyHandCannotDrawWithoutDiscarding() {
        Card top = new Forest();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new KickoffCelebrations()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void hasteAppliesOnlyToOwnPermanentsPresentAtResolution() {
        harness.addToBattlefield(player1, new KickoffCelebrations());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new LoxodonSurveyor());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new LoxodonSurveyor());
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof KickoffCelebrations);
        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isFalse();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new LoxodonSurveyor());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new LoxodonSurveyor());

        assertThat(gqs.hasKeyword(gd, own, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.HASTE)).isFalse();
    }
}