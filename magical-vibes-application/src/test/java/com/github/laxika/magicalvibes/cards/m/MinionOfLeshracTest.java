package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MinionOfLeshracTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent minion(Player owner) {
        UUID id = harness.getPermanentId(owner, "Minion of Leshrac");
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getId().equals(id))
                .findFirst().orElseThrow();
    }

    private Permanent addMinionReady(Player owner) {
        Permanent perm = harness.addToBattlefieldAndReturn(owner, new MinionOfLeshrac());
        perm.setSummoningSick(false);
        return perm;
    }

    // ===== Upkeep: sac another creature or take 5 and tap if damage lands =====

    @Test
    @DisplayName("Declining the sacrifice deals 5 damage and taps the Minion")
    void declineDealsDamageAndTaps() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(minion(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Accepting with a single other creature sacrifices it with no penalty")
    void acceptSacrificesOtherCreatureNoPenalty() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(minion(player1).isTapped()).isFalse();
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
        assertThat(minion(player1).isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
        // Source itself was not sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Minion of Leshrac"));
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player1, new MinionOfLeshrac());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(minion(player1).isTapped()).isFalse();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== {T}: Destroy target creature or land =====

    @Test
    @DisplayName("Activated ability destroys target creature")
    void destroysTargetCreature() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Activated ability destroys target land")
    void destroysTargetLand() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new Forest());

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }

    @Test
    @DisplayName("Cannot target a noncreature, nonland permanent")
    void cannotTargetArtifact() {
        addMinionReady(player1);
        harness.addToBattlefield(player2, new FountainOfYouth());

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target itself (protection from black)")
    void cannotTargetSelf() {
        Permanent self = addMinionReady(player1);
        harness.addToBattlefield(player2, new GrizzlyBears()); // valid alternate so ability is playable

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, self.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
