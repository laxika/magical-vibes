package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.cards.u.UrzasBauble;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinionOfLeshrac.class, BalduvianBears.class, SnowCoveredForest.class, UrzasBauble.class})
class MinionOfLeshracTest extends BaseCardTest {

    private Permanent addMinionReady(Player owner) {
        return addCreatureReady(owner, new MinionOfLeshrac());
    }

    // ===== Upkeep: sac another creature or take 5 and tap if damage lands =====

    @Test
    @DisplayName("Declining the sacrifice deals 5 damage and taps the Minion")
    void declineDealsDamageAndTaps() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new BalduvianBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Minion of Leshrac").isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Accepting with a single other creature sacrifices it with no penalty")
    void acceptSacrificesOtherCreatureNoPenalty() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new BalduvianBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        assertThat(findPermanent(player1, "Minion of Leshrac").isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Accepting with multiple other creatures lets you choose which one to sacrifice")
    void acceptSacrificesChosenOtherCreature() {
        Permanent minion = addMinionReady(player1);
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(firstBear.getId(), secondBear.getId());
        harness.handlePermanentChosen(player1, secondBear.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(minion.getId()))
                .anyMatch(permanent -> permanent.getId().equals(firstBear.getId()))
                .noneMatch(permanent -> permanent.getId().equals(secondBear.getId()));
        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("With no other creature, the penalty applies immediately without a prompt")
    void noOtherCreatureAppliesPenalty() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Minion of Leshrac").isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        // Source itself was not sacrificed
        harness.assertOnBattlefield(player1, "Minion of Leshrac");
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new BalduvianBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Minion of Leshrac").isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @CardUsed({CircleOfProtectionBlack.class})
    @DisplayName("Prevented upkeep damage does not tap the Minion")
    void preventedUpkeepDamageDoesNotTapMinion() {
        Permanent minion = addMinionReady(player1);
        harness.addToBattlefield(player1, new CircleOfProtectionBlack());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, minion.getId());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(minion.isTapped()).isFalse();
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    // ===== {T}: Destroy target creature or land =====

    @Test
    @DisplayName("Activated ability destroys target creature")
    void destroysTargetCreature() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new BalduvianBears());

        var targetId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertInGraveyard(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Activated ability destroys target land")
    void destroysTargetLand() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new SnowCoveredForest());

        var targetId = harness.getPermanentId(player2, "Snow-Covered Forest");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonland permanent")
    void cannotTargetArtifact() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new UrzasBauble());

        var targetId = harness.getPermanentId(player2, "Urza's Bauble");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target itself (protection from black)")
    void cannotTargetSelf() {
        Permanent self = addMinionReady(player1);
        harness.addToBattlefield(player2, new BalduvianBears()); // valid alternate so ability is playable

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, self.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
