package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OverblazeTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles damage from the targeted permanent's activated ability")
    void doublesTargetedPermanentNoncombatDamage() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new AnabaShaman());
        shaman.setSummoningSick(false);
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, shaman.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Doubles combat damage from the targeted permanent")
    void doublesTargetedPermanentCombatDamage() {
        Permanent bears = addReadyBears(player1);
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Only the targeted permanent's damage is doubled")
    void doesNotDoubleOtherPermanentDamage() {
        Permanent targeted = addReadyBears(player1);
        Permanent other = addReadyBears(player1);
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, targeted.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0, 1));

        assertThat(gd.getLife(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Can be spliced onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Permanent giant = new Permanent(new HillGiant());
        giant.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(giant);
        Card arcaneSpell = new Shock().createRuntimeCopy();
        arcaneSpell.setSubtypes(List.of(CardSubtype.ARCANE));
        Overblaze overblaze = new Overblaze();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(arcaneSpell, overblaze));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castWithSplice(player1, 0, giant.getId(), List.of(1));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(overblaze);
    }

    @Test
    @DisplayName("The doubling wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bears = addReadyBears(player2);
        harness.setHand(player1, List.of(new Overblaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    private Permanent addReadyBears(com.github.laxika.magicalvibes.model.Player player) {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(bears);
        return bears;
    }
}
