package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HourOfDevastation;
import com.github.laxika.magicalvibes.cards.i.IronrootTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagmaticGalleon.class, GrizzlyBears.class, HillGiant.class, HourOfDevastation.class,
        IronrootTreefolk.class})
class MagmaticGalleonTest extends BaseCardTest {

    @Test
    void entersAndCreatesTreasureWhenItsDamageIsExcess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new MagmaticGalleon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotCreateTreasureWhenDamageIsExactlyLethal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IronrootTreefolk());
        harness.setHand(player1, List.of(new MagmaticGalleon()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ironroot Treefolk");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void createsOnlyOneTreasureWhenSeveralCreaturesAreDealtExcessDamageByOneEvent() {
        harness.addToBattlefield(player1, new MagmaticGalleon());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HourOfDevastation()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerForExcessCombatDamage() {
        harness.addToBattlefield(player1, new MagmaticGalleon());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
