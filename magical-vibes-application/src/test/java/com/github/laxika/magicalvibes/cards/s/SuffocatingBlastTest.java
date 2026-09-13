package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnaDisciple;
import com.github.laxika.magicalvibes.cards.c.CetaSanctuary;
import com.github.laxika.magicalvibes.cards.c.Cromat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SuffocatingBlast.class, AnaDisciple.class, CetaSanctuary.class, Cromat.class})
class SuffocatingBlastTest extends BaseCardTest {

    @Test
    void countersCreatureSpellAndDealsDamageToCreature() {
        Permanent targetCreature = addCreatureReady(player1, new Cromat());
        AnaDisciple targetSpell = new AnaDisciple();

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castFromHand(player1, targetSpell, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ana Disciple");
        assertThat(targetCreature.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Cromat");
    }

    @Test
    void countersNonCreatureSpellAndDealsDamageToCreature() {
        Permanent targetCreature = addCreatureReady(player1, new AnaDisciple());
        CetaSanctuary targetSpell = new CetaSanctuary();

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castFromHand(player1, targetSpell, "{2}{U}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ceta Sanctuary");
        harness.assertInGraveyard(player1, "Ana Disciple");
    }

    @Test
    void stillDealsDamageIfSpellTargetIsNoLongerOnStack() {
        Permanent targetCreature = addCreatureReady(player1, new AnaDisciple());
        AnaDisciple targetSpell = new AnaDisciple();

        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.castFromHand(player1, targetSpell, "{G}");
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetSpell.getId(), targetCreature.getId());
        gd.stack.removeIf(entry -> entry.getCard().getId().equals(targetSpell.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ana Disciple");
    }

    @Test
    void cannotTargetPermanentAsSpellTarget() {
        Permanent targetCreature = addCreatureReady(player1, new Cromat());
        harness.setHand(player2, List.of(new SuffocatingBlast()));
        addBlastMana();

        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(
                player2, 0, targetCreature.getId(), targetCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addBlastMana() {
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
    }
}
