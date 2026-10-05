package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronManArmor;
import com.github.laxika.magicalvibes.cards.s.SlipperyBogle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillmongerScourgeOfWakanda.class, KunLunWarrior.class, Forest.class,
        IronManArmor.class, SlipperyBogle.class})
class KillmongerScourgeOfWakandaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may sacrifice another creature to destroy an opponent's nonland permanent")
    void etbSacrificeDestroysOpponentsNonlandPermanent() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KunLunWarrior());
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
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KunLunWarrior());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(sacrifice);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Killmonger gets +2/+1 with two creature cards in its controller's graveyard")
    void graveyardThresholdBoostsKillmonger() {
        harness.setGraveyard(player1, List.of(new KunLunWarrior(), new KunLunWarrior()));
        harness.addToBattlefield(player1, new KillmongerScourgeOfWakanda());

        Permanent killmonger = findPermanent(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(4);
    }

    @Test
    @DisplayName("Noncreature cards and an opponent's graveyard do not satisfy the threshold")
    void unrelatedGraveyardCardsDoNotBoostKillmonger() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new KunLunWarrior(), new KunLunWarrior()));
        harness.addToBattlefield(player1, new KillmongerScourgeOfWakanda());

        Permanent killmonger = findPermanent(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(3);
    }

    @Test
    @DisplayName("Killmonger cannot sacrifice itself when it is the only creature controlled")
    void noOtherCreatureMeansNoDestruction() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KunLunWarrior());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature may be sacrificed even when the opponent has no nonland target")
    void sacrificeWithoutDestructionTargetStillHappens() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "K'un-Lun Warrior");
        harness.assertNotOnBattlefield(player1, "K'un-Lun Warrior");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    @DisplayName("The sacrifice can turn on the graveyard bonus before the destruction trigger resolves")
    void sacrificeTurnsOnGraveyardBonus() {
        harness.setGraveyard(player1, List.of(new KunLunWarrior()));
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KunLunWarrior());

        castKillmonger();
        Permanent killmonger = findPermanent(player1, "Killmonger, Scourge of Wakanda");
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(3);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(4);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new KunLunWarrior(), new Forest()));
        assertThat(gqs.getEffectivePower(gd, killmonger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, killmonger)).isEqualTo(3);
    }

    @Test
    @DisplayName("The reflexive destruction trigger cannot target an opponent's creature with hexproof")
    void hexproofCreatureIsNotALegalDestructionTarget() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new KunLunWarrior());
        Permanent hexproof = harness.addToBattlefieldAndReturn(player2, new SlipperyBogle());

        castKillmonger();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();

        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hexproof);
    }

    @Test
    @DisplayName("Destruction can target noncreature artifacts but cannot target your own permanents")
    void destroysOpposingNoncreatureArtifact() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new KunLunWarrior());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new IronManArmor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronManArmor());

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

        harness.assertNotOnBattlefield(player2, "Iron Man Armor");
        harness.assertInGraveyard(player2, "Iron Man Armor");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    private void castKillmonger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new KillmongerScourgeOfWakanda(), "{2}{B}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
