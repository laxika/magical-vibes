package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VectisDominatorTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays 2 life to keep the creature untapped")
    void controllerPaysLifeCreatureStaysUntapped() {
        addReadyDominator(player1);
        Permanent target = addReadyBears(player2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller declines and the creature is tapped")
    void controllerDeclinesCreatureTapped() {
        addReadyDominator(player1);
        Permanent target = addReadyBears(player2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        harness.assertLife(player2, 20);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A controller with too little life can't pay and the creature is tapped automatically")
    void cannotPayTapsAutomatically() {
        addReadyDominator(player1);
        Permanent target = addReadyBears(player2);
        harness.setLife(player2, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        // No choice offered — the controller can't pay 2 life, so the creature is tapped outright.
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertLife(player2, 1);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability fizzles if the target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        addReadyDominator(player1);
        Permanent target = addReadyBears(player2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNoncreature() {
        addReadyDominator(player1);
        Permanent forest = addReadyForest(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDominator(Player player) {
        Permanent perm = new Permanent(new VectisDominator());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
