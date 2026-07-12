package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RunedHaloTest extends BaseCardTest {

    // ===== Card name choice on enter =====

    @Test
    @DisplayName("Resolving Runed Halo awaits a card name choice and records it on the permanent")
    void resolvingChoosesCardName() {
        harness.setHand(player1, List.of(new RunedHalo()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Shock");

        Permanent halo = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Runed Halo"))
                .findFirst().orElseThrow();
        assertThat(halo.getChosenName()).isEqualTo("Shock");
    }

    // ===== Protection from targeting =====

    @Test
    @DisplayName("A spell with the chosen name can't target the protected player")
    void chosenNameSpellCannotTargetPlayer() {
        addReadyRunedHalo(player1, "Shock");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection is player-scoped: the named spell can still target a permanent the player controls")
    void chosenNameSpellCanStillTargetPlayersPermanent() {
        addReadyRunedHalo(player1, "Shock");
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A spell with a different name can still target the protected player")
    void differentNameSpellCanTargetPlayer() {
        addReadyRunedHalo(player1, "Lightning Bolt");
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Protection from combat damage =====

    @Test
    @DisplayName("A creature with the chosen name deals no combat damage to the protected player")
    void chosenNameAttackerDealsNoCombatDamage() {
        addReadyRunedHalo(player1, "Grizzly Bears");
        harness.setLife(player1, 20);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A creature with a different name still deals combat damage to the protected player")
    void differentNameAttackerDealsCombatDamage() {
        addReadyRunedHalo(player1, "Hill Giant");
        harness.setLife(player1, 20);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    // ===== Helpers =====

    private Permanent addReadyRunedHalo(Player player, String chosenName) {
        Permanent perm = new Permanent(new RunedHalo());
        perm.setChosenName(chosenName);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
