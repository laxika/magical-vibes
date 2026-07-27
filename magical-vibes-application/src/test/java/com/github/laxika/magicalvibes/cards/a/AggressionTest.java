package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.Keyword;
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

class AggressionTest extends BaseCardTest {

    private Permanent attach(Player auraController, Permanent host) {
        Permanent aura = new Permanent(new Aggression());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
        return aura;
    }

    private Permanent addCreature(Player owner) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(owner.getId()).add(perm);
        return perm;
    }

    private void runEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Enchanted creature has first strike and trample")
    void grantsFirstStrikeAndTrample() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is destroyed at its controller's end step if it didn't attack")
    void destroysNonAttacker() {
        Permanent bears = addCreature(player1);
        attach(player1, bears);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature survives if it attacked this turn")
    void sparesAttacker() {
        Permanent bears = addCreature(player1);
        bears.setAttackedThisTurn(true);
        attach(player1, bears);

        runEndStep(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Trigger only fires on the enchanted creature's controller's end step")
    void doesNotFireOnOtherPlayersEndStep() {
        Permanent bears = addCreature(player2);
        attach(player1, bears);

        runEndStep(player1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);

        runEndStep(player2);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Cannot enchant a Wall")
    void cannotEnchantWall() {
        harness.addToBattlefield(player1, new WallOfAir());
        harness.setHand(player1, List.of(new Aggression()));
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent wall = findPermanent(player1, "Wall of Air");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, wall.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
