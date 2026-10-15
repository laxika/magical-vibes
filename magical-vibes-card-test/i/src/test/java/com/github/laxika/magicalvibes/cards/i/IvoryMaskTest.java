package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.OrcishArtillery;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IvoryMask.class, LavaAxe.class, AnabaShaman.class, Millstone.class, Shock.class, OrcishArtillery.class})
class IvoryMaskTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Ivory Mask puts it onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new IvoryMask()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Ivory Mask");
    }

    @Test
    @DisplayName("Opponent cannot target the controller with a spell while Ivory Mask is out")
    void opponentCannotTargetControllerWithSpell() {
        harness.addToBattlefield(player1, new IvoryMask());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LavaAxe()));
        harness.addMana(player2, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Controller cannot target themselves either while Ivory Mask is out")
    void controllerCannotTargetSelf() {
        harness.addToBattlefield(player1, new IvoryMask());
        harness.setHand(player1, List.of(new LavaAxe()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Opponent cannot target the controller with an ability while Ivory Mask is out")
    void opponentCannotTargetControllerWithAbility() {
        harness.addToBattlefield(player1, new IvoryMask());
        addCreatureReady(player2, new AnabaShaman());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Controller can be targeted again after Ivory Mask leaves the battlefield")
    void canTargetControllerAfterRemoval() {
        IvoryMask mask = new IvoryMask();
        Permanent perm = harness.addToBattlefieldAndReturn(player1, mask);
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(mask);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new LavaAxe()));
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Shroud also prevents activated abilities from targeting the controller")
    void activatedAbilityCannotTargetController() {
        harness.addToBattlefield(player1, new Millstone());
        harness.addToBattlefield(player2, new IvoryMask());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud makes an already targeted player illegal on resolution")
    void gainingShroudBeforeResolutionStopsSpell() {
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        harness.addToBattlefield(player1, new IvoryMask());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ivory Mask on the stack does not grant shroud yet")
    void spellCanTargetControllerBeforeMaskResolves() {
        harness.setHand(player1, List.of(new IvoryMask()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());

        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Ivory Mask");
    }

    @Test
    @DisplayName("Ivory Mask does not protect creatures its controller controls")
    void controlledCreatureCanStillBeTargeted() {
        harness.addToBattlefield(player1, new IvoryMask());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AnabaShaman());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Anaba Shaman");
        harness.assertOnBattlefield(player1, "Ivory Mask");
    }

    @Test
    @DisplayName("Shroud does not prevent untargeted damage to the controller")
    void untargetedDamageStillAffectsController() {
        harness.addToBattlefield(player1, new IvoryMask());
        addCreatureReady(player1, new OrcishArtillery());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 18);
    }
}
