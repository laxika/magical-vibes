package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.FlamesOfTheBloodHand;
import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.ForkedBranchGarami;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.h.HeartlessHidetsugu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Overblaze.class, HeartlessHidetsugu.class, GnarledMass.class,
        FirstVolley.class, FlamesOfTheBloodHand.class, ForkedBranchGarami.class})
class OverblazeTest extends BaseCardTest {

    @Test
    @DisplayName("Two Overblazes quadruple the same permanent's damage")
    void multipleOverblazesStack() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new Overblaze(), new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Doubles damage dealt to creatures as well as players")
    void doublesCombatDamageToCreature() {
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player2, new ForkedBranchGarami());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Forked-Branch Garami");
        harness.assertInGraveyard(player1, "Gnarled Mass");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not double damage received by the target or dealt by spells")
    void doesNotDoubleIncomingSpellDamage() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gnarled Mass");
        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles damage from the targeted permanent's noncombat ability")
    void doublesTargetedPermanentNoncombatDamage() {
        Permanent hidetsugu = addCreatureReady(player1, new HeartlessHidetsugu());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 21);
        harness.setLife(player2, 21);

        harness.castInstant(player1, 0, hidetsugu.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles combat damage from the targeted permanent")
    void doublesTargetedPermanentCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Only the targeted permanent's damage is doubled")
    void doesNotDoubleOtherPermanentDamage() {
        Permanent targeted = addCreatureReady(player1, new GnarledMass());
        addCreatureReady(player1, new GnarledMass());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, targeted.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0, 1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(11);
    }

    @Test
    @DisplayName("Can be spliced onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent giant = addCreatureReady(player2, new GnarledMass());
        FirstVolley arcaneSpell = new FirstVolley();
        Overblaze overblaze = new Overblaze();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneSpell, overblaze));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player1, 20);

        harness.castWithSplice(player1, 0, giant.getId(), List.of(1));
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(overblaze);
    }

    @Test
    @DisplayName("Cannot be spliced onto a non-Arcane spell")
    void rejectsNonArcaneHost() {
        harness.setHand(player1, List.of(new FlamesOfTheBloodHand(), new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 7);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, player2.getId(), List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot be spliced");
    }

    @Test
    @DisplayName("The doubling wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }
}
