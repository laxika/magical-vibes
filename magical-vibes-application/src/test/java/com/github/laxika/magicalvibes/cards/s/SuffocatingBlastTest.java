package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuffocatingBlast.class, GrizzlyBears.class, HillGiant.class, MightOfOaks.class})
class SuffocatingBlastTest extends BaseCardTest {

    @Test
    void countersCreatureSpellAndDealsDamageToCreature() {
        HillGiant targetCreature = new HillGiant();
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.addToBattlefield(player1, targetCreature);
        var targetCreatureId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void countersNonCreatureSpellAndDealsDamageToCreature() {
        HillGiant targetCreature = new HillGiant();
        MightOfOaks targetSpell = new MightOfOaks();
        harness.addToBattlefield(player1, targetCreature);
        var targetCreatureId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castInstant(player1, 0, targetCreatureId);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreatureId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void stillDealsDamageIfSpellTargetIsNoLongerOnStack() {
        HillGiant targetCreature = new HillGiant();
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.addToBattlefield(player1, targetCreature);
        var targetCreatureId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreatureId);
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(targetSpell.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hill Giant");
    }

    @Test
    void cannotTargetPermanentAsSpellTarget() {
        HillGiant targetCreature = new HillGiant();
        harness.addToBattlefield(player1, targetCreature);
        var targetCreatureId = harness.getPermanentId(player1, "Hill Giant");
        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, targetCreatureId, targetCreatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlastMana() {
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
