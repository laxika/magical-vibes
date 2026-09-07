package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VampireWarlordTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants a regeneration shield")
    void sacrificingAnotherCreatureGrantsShield() {
        addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Vampire Warlord").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate when Vampire Warlord is the only creature")
    void cannotSacrificeItself() {
        addWarlordReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
    }

    @Test
    @DisplayName("Choosing Vampire Warlord itself as the sacrifice is rejected")
    void choosingItselfIsRejected() {
        Permanent warlord = addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, warlord.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Regeneration shield saves Vampire Warlord from lethal combat damage")
    void regenSavesFromLethalCombat() {
        Permanent warlord = addWarlordReady(player1);
        warlord.setRegenerationShield(1);
        warlord.setBlocking(true);
        warlord.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vampire Warlord");
        Permanent survivor = findPermanent(player1, "Vampire Warlord");
        assertThat(survivor.isTapped()).isTrue();
        assertThat(survivor.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Vampire Warlord dies without a regeneration shield")
    void diesWithoutRegenShield() {
        Permanent warlord = addWarlordReady(player1);
        warlord.setBlocking(true);
        warlord.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vampire Warlord");
    }

    @Test
    @DisplayName("Chosen creature is sacrificed when several are available")
    void chosenCreatureIsSacrificed() {
        addWarlordReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Vampire Warlord").getRegenerationShield()).isEqualTo(1);
    }

    private Permanent addWarlordReady(Player player) {
        Permanent perm = new Permanent(new VampireWarlord());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
