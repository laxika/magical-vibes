package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.ArcaneFlight;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeElementalTest extends BaseCardTest {

    // ===== Attack / block delayed sacrifice + self damage =====

    @Test
    @DisplayName("Attacking sacrifices Time Elemental and deals 5 damage to its controller at end of combat")
    void attackingSacrificesAndDealsFiveToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent elemental = new Permanent(new TimeElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(elemental);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Time Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Time Elemental"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Blocking sacrifices Time Elemental and deals 5 damage to its controller at end of combat")
    void blockingSacrificesAndDealsFiveToController() {
        harness.setLife(player2, 20);

        Permanent elemental = new Permanent(new TimeElemental());
        elemental.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(elemental);

        // 1/1 attacker so Time Elemental (0/2) survives combat damage and reaches the end-of-combat sacrifice.
        Permanent attacker = new Permanent(new FugitiveWizard());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Time Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Time Elemental"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    // ===== Activated bounce ability =====

    @Test
    @DisplayName("Activated ability returns a target permanent that isn't enchanted to its owner's hand")
    void activatedAbilityBouncesTargetPermanent() {
        harness.addToBattlefield(player1, new TimeElemental());
        findPermanent(player1, "Time Elemental").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Activated ability cannot target an enchanted permanent")
    void activatedAbilityCannotTargetEnchantedPermanent() {
        harness.addToBattlefield(player1, new TimeElemental());
        findPermanent(player1, "Time Elemental").setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(bears);
        Permanent aura = new Permanent(new ArcaneFlight());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
