package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WingedBoots.class, GrizzlyBears.class, Shock.class})
class WingedBootsTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Winged Boots gives the creature flying")
    void equippingGivesFlying() {
        Permanent boots = addBootsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boots.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ward counters an opponent's spell when they do not pay {4}")
    void wardCountersUnpaidSpell() {
        Permanent creature = addEquippedCreature(player1);
        castShockAtCreature(creature);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Paying {4} lets an opponent's spell targeting the equipped creature resolve")
    void payingWardLetsSpellResolve() {
        Permanent creature = addEquippedCreature(player1);
        castShockAtCreature(creature);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    private Permanent addEquippedCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        Permanent boots = addBootsReady(player);
        boots.setAttachedTo(creature.getId());
        return creature;
    }

    private void castShockAtCreature(Permanent creature) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, creature.getId());
    }

    private Permanent addBootsReady(Player player) {
        Permanent boots = harness.addToBattlefieldAndReturn(player, new WingedBoots());
        boots.setSummoningSick(false);
        return boots;
    }
}
