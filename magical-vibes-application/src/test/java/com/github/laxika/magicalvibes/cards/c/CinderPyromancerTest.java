package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CinderPyromancerTest extends BaseCardTest {

    // ===== Activated ability: {T}: deal 1 damage to target player =====

    @Test
    @DisplayName("Tap ability deals 1 damage to target player")
    void tapAbilityDealsDamageToPlayer() {
        addReadyPyromancer(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Tap ability requires tap — cannot activate when already tapped")
    void tapAbilityRequiresTap() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    // ===== Red spell cast trigger: may untap =====

    @Test
    @DisplayName("Casting a red spell lets you untap this creature")
    void redSpellUntapsPyromancer() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve untap trigger

        assertThat(pyromancer.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves this creature tapped")
    void decliningLeavesPyromancerTapped() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(pyromancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-red spell does not trigger the untap")
    void nonRedSpellDoesNotTrigger() {
        Permanent pyromancer = addReadyPyromancer(player1);
        pyromancer.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(pyromancer.isTapped()).isTrue();
    }

    // ===== Helper =====

    private Permanent addReadyPyromancer(Player player) {
        Permanent perm = new Permanent(new CinderPyromancer());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
