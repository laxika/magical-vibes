package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinBalloonBrigade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UnyaroGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a red instant spell, sacrificing itself as a cost")
    void countersRedInstantSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock is countered into player2's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        // Unyaro Griffin sacrificed as a cost
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Unyaro Griffin"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Unyaro Griffin"));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-red instant spell")
    void cannotTargetGreenInstant() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        harness.addToBattlefield(player2, new GrizzlyBears());

        GiantGrowth giantGrowth = new GiantGrowth();
        harness.setHand(player2, List.of(giantGrowth));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giantGrowth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a red creature spell (not an instant or sorcery)")
    void cannotTargetRedCreatureSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        GoblinBalloonBrigade goblin = new GoblinBalloonBrigade();
        harness.setHand(player2, List.of(goblin));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
