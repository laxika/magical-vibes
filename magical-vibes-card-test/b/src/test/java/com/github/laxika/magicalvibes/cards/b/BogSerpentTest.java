package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BogSerpent.class, Swamp.class, Forest.class, UrborgTombOfYawgmoth.class})
class BogSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Swamps")
    void sacrificedWhenNoSwamps() {
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Survives while controller controls a Swamp")
    void survivesWithSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Survives while Urborg makes a controlled land a Swamp")
    void survivesWithUrborgGrantingSwampSubtype() {
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("A non-Swamp land does not satisfy the state trigger")
    void nonSwampLandDoesNotSatisfyStateTrigger() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Can attack when defending player controls a Swamp")
    void canAttackWhenDefenderControlsSwamp() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        Permanent serpent = addCreatureReady(player1, new BogSerpent());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        declareAttackers(List.of(index));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Swamp")
    void cannotAttackWhenDefenderHasNoSwamp() {
        harness.addToBattlefield(player1, new Swamp());
        Permanent serpent = addCreatureReady(player1, new BogSerpent());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        assertThatThrownBy(() -> declareAttackers(List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Swamp does not prevent the sacrifice trigger")
    void opponentsSwampDoesNotPreventSacrifice() {
        harness.addToBattlefield(player2, new Swamp());
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bog Serpent");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
    }

    @Test
    @DisplayName("Gaining a Swamp after the ability triggers does not stop the sacrifice")
    void gainingSwampAfterTriggerDoesNotPreventSacrifice() {
        harness.setHand(player1, List.of(new BogSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bog Serpent");
        assertThat(gd.stack).hasSize(1);
        harness.addToBattlefield(player1, new Swamp());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bog Serpent");
        harness.assertInGraveyard(player1, "Bog Serpent");
        harness.assertOnBattlefield(player1, "Swamp");
    }

    @Test
    @DisplayName("Can attack when Urborg makes the defender's Forest a Swamp")
    void canAttackWhenUrborgGrantsDefendersLandSwampSubtype() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new UrborgTombOfYawgmoth());
        harness.addToBattlefield(player2, new Forest());
        Permanent serpent = addCreatureReady(player1, new BogSerpent());

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(serpent);
        declareAttackers(List.of(index));

        harness.assertLife(player2, 15);
    }
}
