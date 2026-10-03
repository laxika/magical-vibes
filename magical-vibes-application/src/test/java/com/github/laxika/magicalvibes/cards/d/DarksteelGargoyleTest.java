package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingDecay;
import com.github.laxika.magicalvibes.cards.o.Oxidize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarksteelGargoyle.class, CrazedGoblin.class, Oxidize.class, EchoingDecay.class})
class DarksteelGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Darksteel Gargoyle cannot be blocked by a creature without flying")
    void cannotBeBlockedByNonFlyingCreature() {
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        attacker.setAttacking(true);
        addCreatureReady(player2, new CrazedGoblin());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Darksteel Gargoyle (flying)");
    }

    @Test
    @DisplayName("Darksteel Gargoyle survives a destroy effect")
    void survivesDestroyEffect() {
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());

        harness.setHand(player1, List.of(new Oxidize()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player2, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Flying Gargoyles can block each other and survive lethal combat damage")
    void flyingBlockerAndAttackerSurviveLethalCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new DarksteelGargoyle());
        Permanent blocker = addCreatureReady(player2, new DarksteelGargoyle());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Darksteel Gargoyle");
        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player1, "Darksteel Gargoyle");
        harness.assertNotInGraveyard(player2, "Darksteel Gargoyle");
    }

    @Test
    @DisplayName("Indestructible does not save Darksteel Gargoyle from zero or negative toughness")
    void diesWhenToughnessIsReducedBelowZero() {
        Permanent gargoyle = addCreatureReady(player2, new DarksteelGargoyle());
        harness.setHand(player1, List.of(new EchoingDecay(), new EchoingDecay()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveInstant(player1, 0, gargoyle.getId());
        harness.assertOnBattlefield(player2, "Darksteel Gargoyle");
        harness.castAndResolveInstant(player1, 0, gargoyle.getId());

        harness.assertNotOnBattlefield(player2, "Darksteel Gargoyle");
        harness.assertInGraveyard(player2, "Darksteel Gargoyle");
    }
}
