package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FodderLaunchTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -5/-5 and deals 5 to its controller")
    void minusFiveAndDamageToController() {
        Permanent goblin = new Permanent(new SkirkProspector());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        Permanent target = new Permanent(new GrizzlyBears()); // 2/2
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        // -5/-5 kills the 2/2
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Sacrificed Goblin went to its controller's graveyard
        harness.assertInGraveyard(player1, "Skirk Prospector");
        // Controller of the targeted creature takes 5 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("The -5/-5 wears off at end of turn but the damage remains")
    void minusFiveWearsOff() {
        Permanent goblin = new Permanent(new SkirkProspector());
        gd.playerBattlefields.get(player1.getId()).add(goblin);

        Permanent target = new Permanent(new AvatarOfMight()); // 8/8, survives -5/-5
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), goblin.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-5);
        assertThat(target.getToughnessModifier()).isEqualTo(-5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot cast without a Goblin to sacrifice")
    void cannotCastWithoutGoblin() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FodderLaunch()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }
}
