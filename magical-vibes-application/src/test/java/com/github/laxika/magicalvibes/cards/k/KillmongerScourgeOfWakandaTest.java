package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillmongerScourgeOfWakanda.class, GrizzlyBears.class, Forest.class})
class KillmongerScourgeOfWakandaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy an opponent's nonland permanent")
    void etbSacrificeDestroysOpponentsNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice sacrificeChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(sacrificeChoice.validIds()).containsExactly(sacrifice.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice targetChoice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(targetChoice.validIds()).containsExactly(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getId().equals(sacrifice.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(
                permanent -> permanent.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("Declining the ETB sacrifice does nothing")
    void decliningSacrificeDoesNothing() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Killmonger gets +2/+1 with two creature cards in its controller's graveyard")
    void graveyardThresholdBoostsKillmonger() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new KillmongerScourgeOfWakanda());

        Permanent killmonger = findPermanent(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Noncreature cards and an opponent's graveyard do not satisfy the threshold")
    void unrelatedGraveyardCardsDoNotBoostKillmonger() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new KillmongerScourgeOfWakanda());

        Permanent killmonger = findPermanent(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(3);
    }

    private void castKillmonger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KillmongerScourgeOfWakanda()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
