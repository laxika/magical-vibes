package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GolgariRotwurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildmage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeningBanshee.class, GolgariRotwurm.class, SelesnyaGuildmage.class, Forest.class})
class KeeningBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature -2/-2")
    void etbWeakensTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB can target your own creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GolgariRotwurm());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB puts a zero-toughness creature into its graveyard")
    void lethalDebuffKillsTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildmage()).getId();

        castKeeningBanshee(targetId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Selesnya Guildmage");
        harness.assertInGraveyard(player2, "Selesnya Guildmage");
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new SelesnyaGuildmage()).getId();

        castKeeningBanshee(targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Must target itself when it is the only creature")
    void targetsItselfOnAnEmptyBattlefield() {
        harness.castFromHand(player1, new KeeningBanshee(), "{2}{B}{B}");
        harness.passBothPriorities();

        UUID bansheeId = harness.getPermanentId(player1, "Keening Banshee");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, bansheeId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Keening Banshee");
        harness.assertInGraveyard(player1, "Keening Banshee");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature with its triggered ability")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new KeeningBanshee(), "{2}{B}{B}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The triggered ability resolves after its source leaves")
    void triggerResolvesWithoutItsSource() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        castKeeningBanshee(target.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("A target chosen after entry does not make the creature spell targeted")
    void choosesTargetOnlyAfterEntering() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GolgariRotwurm());
        harness.castFromHand(player1, new KeeningBanshee(), "{2}{B}{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Keening Banshee");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Keening Banshee");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    private void castKeeningBanshee(UUID targetId) {
        harness.castFromHand(player1, new KeeningBanshee(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
    }
}