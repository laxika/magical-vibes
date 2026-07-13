package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RosheenMeandererTest extends BaseCardTest {

    private void activateManaAbility() {
        Permanent rosheen = gd.playerBattlefields.get(player1.getId()).getFirst();
        rosheen.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);
    }

    // ===== Mana production =====

    @Test
    @DisplayName("Tap ability adds four x-cost-only colorless mana")
    void tapAbilityAddsFourXCostOnlyColorless() {
        harness.addToBattlefield(player1, new RosheenMeanderer());

        activateManaAbility();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);
    }

    // ===== Spending restriction: costs that contain {X} =====

    @Test
    @DisplayName("X-cost-only mana pays the generic X portion of an X spell")
    void xCostOnlyManaPaysXSpell() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Fireball {X}{R}: X=4 → 4 generic (paid by x-cost-only) + {R} (paid by red).
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Fireball()));
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 4, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("X-cost-only mana cannot pay a spell whose cost contains no {X}")
    void xCostOnlyManaCannotPayNonXSpell() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // Grizzly Bears {1}{G}: no {X} in cost, so the generic {1} can't come from x-cost-only mana.
        // Only 1 green available → not enough.
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);
    }

    @Test
    @DisplayName("X-cost-only mana drains at phase transition")
    void xCostOnlyManaDrainsAtPhaseTransition() {
        harness.addToBattlefield(player1, new RosheenMeanderer());
        activateManaAbility();
        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(4);

        gd.playerManaPools.get(player1.getId()).drainNonPersistent();

        assertThat(gd.playerManaPools.get(player1.getId()).getXCostOnlyColorless()).isEqualTo(0);
    }
}
