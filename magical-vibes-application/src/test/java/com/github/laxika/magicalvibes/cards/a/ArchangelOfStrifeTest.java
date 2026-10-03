package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArchangelOfStrife.class, GrizzlyBears.class})
class ArchangelOfStrifeTest extends BaseCardTest {

    @Test
    void eachPlayerChoosesAndTheirCreaturesGetTheMatchingBoost() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ArchangelOfStrife()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "War");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "Peace");

        Permanent archangel = findPermanent(player1, "Archangel of Strife");
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, archangel)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, archangel)).isEqualTo(6);
    }

    @Test
    void bothPlayersCanChooseWar() {
        Permanent archangel = harness.enterBattlefieldAndReturn(player1, new ArchangelOfStrife());
        harness.handleListChoice(player1, "War");
        harness.handleListChoice(player2, "War");

        Permanent opponentBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, archangel)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, archangel)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void bothPlayersCanChoosePeace() {
        Permanent archangel = harness.enterBattlefieldAndReturn(player1, new ArchangelOfStrife());
        harness.handleListChoice(player1, "Peace");
        harness.handleListChoice(player2, "Peace");

        Permanent opponentBear = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, archangel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, archangel)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void multipleArchangelsKeepIndependentChoicesAndBoostsEndWhenTheyLeave() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new ArchangelOfStrife());
        harness.handleListChoice(player1, "War");
        harness.handleListChoice(player2, "Peace");

        Permanent second = harness.enterBattlefieldAndReturn(player2, new ArchangelOfStrife());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Peace");
        harness.handleListChoice(player2, "War");

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(9);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(9);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, first));

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
    }

    @Test
    void activePlayerChoosesFirstEvenWhenTheOtherPlayerControlsTheArchangel() {
        harness.forceActivePlayer(player2);
        Permanent archangel = harness.enterBattlefieldAndReturn(player1, new ArchangelOfStrife());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "War");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "Peace");

        assertThat(gqs.getEffectivePower(gd, archangel)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, archangel)).isEqualTo(9);
    }

    @Test
    void returningArchangelRequiresNewChoices() {
        harness.setHand(player1, List.of());
        Permanent archangel = harness.enterBattlefieldAndReturn(player1, new ArchangelOfStrife());
        harness.handleListChoice(player1, "War");
        harness.handleListChoice(player2, "Peace");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, archangel));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Peace");
        harness.handleListChoice(player2, "War");

        Permanent returned = findPermanent(player1, "Archangel of Strife");
        assertThat(returned.getId()).isNotEqualTo(archangel.getId());
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(9);
    }
}
