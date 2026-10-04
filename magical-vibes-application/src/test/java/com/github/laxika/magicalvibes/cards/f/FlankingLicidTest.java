package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlankingLicid.class, TrainedArmodon.class, Forest.class})
class FlankingLicidTest extends BaseCardTest {

    @Test
    @DisplayName("Ability attaches the Licid to the target creature and grants flanking")
    void abilityTurnsLicidIntoAttachedAuraThatGrantsFlanking() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(licid.getCard().isAura()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isFalse();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLANKING)).isTrue();
    }

    @Test
    @DisplayName("Paying the end cost detaches the Licid and removes flanking")
    void endCostRevertsLicidToCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(licid.getAttachedTo()).isNull();
        assertThat(licid.getCard().isAura()).isFalse();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLANKING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetLand() {
        addReadyLicid(player1);
        Permanent land = addReadyLand(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    @Test
    void canEnchantOpponentsCreature() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLANKING)).isTrue();
        harness.assertOnBattlefield(player1, "Flanking Licid");
    }

    @Test
    void endingEffectRequiresRedManaAndPreservesTapStatus() {
        Permanent licid = addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(licid.getAttachedTo()).isEqualTo(host.getId());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(licid.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, licid)).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void selfTargetCannotRemainOnBattlefieldAsAnAura() {
        Permanent licid = addReadyLicid(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, licid.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Flanking Licid");
        harness.assertInGraveyard(player1, "Flanking Licid");
    }

    @Test
    void grantedFlankingShrinksBlockerAndTriggerSurvivesEndingEffect() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        host.setAttacking(true);
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))));
        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FLANKING)).isFalse();
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void gainingFlankingAfterBlockersAreDeclaredDoesNotShrinkExistingBlocker() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        host.setAttacking(true);
        prepareDeclareBlockers();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, host, Keyword.FLANKING)).isTrue();
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void grantedFlankingDoesNotPenalizeBlockerWithFlanking() {
        addReadyLicid(player1);
        Permanent host = addCreatureReady(player1, new TrainedArmodon());
        addReadyLicid(player2);
        Permanent blocker = addCreatureReady(player2, new TrainedArmodon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, host.getId());
        harness.passBothPriorities();
        harness.addMana(player2, ManaColor.RED, 1);
        harness.activateAbility(player2, 0, null, blocker.getId());
        harness.passBothPriorities();
        host.setAttacking(true);
        prepareDeclareBlockers();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1))));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addReadyLicid(Player player) {
        return addCreatureReady(player, new FlankingLicid());
    }
}
