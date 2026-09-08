package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Fruition;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TreetopDefense;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticDenial.class, GrizzlyBears.class, Fruition.class, TreetopDefense.class})
class MysticDenialTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a creature spell")
    void countersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Counters a sorcery spell")
    void countersSorcerySpell() {
        Fruition fruition = new Fruition();
        harness.setHand(player1, List.of(fruition));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setHand(player2, List.of(new MysticDenial()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, fruition.getId());

        harness.assertInGraveyard(player1, "Fruition");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an instant spell")
    void cannotTargetInstantSpell() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        TreetopDefense treetopDefense = new TreetopDefense();
        harness.setHand(player2, List.of(treetopDefense));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0);

        MysticDenial mysticDenial = new MysticDenial();
        harness.setHand(player1, List.of(mysticDenial));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, treetopDefense.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target spell leaves the stack before resolution")
    void fizzlesIfTargetSpellRemoved() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        MysticDenial mysticDenial = new MysticDenial();
        harness.setHand(player2, List.of(mysticDenial));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(bears.getId()));

        harness.passBothPriorities();

        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertInGraveyard(player2, "Mystic Denial");
        assertThat(gd.stack).isEmpty();
    }
}
