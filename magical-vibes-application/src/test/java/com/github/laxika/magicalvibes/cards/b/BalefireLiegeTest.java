package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BalefireLiegeTest extends BaseCardTest {

    // ===== Static effects: +1/+1 to own red / white creatures =====

    @Test
    @DisplayName("Other red creatures you control get +1/+1")
    void buffsOwnRedCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.addToBattlefield(player1, new HillGiant());

        Permanent giant = findPermanent(player1, "Hill Giant");
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(4);
    }

    @Test
    @DisplayName("Other white creatures you control get +1/+1")
    void buffsOwnWhiteCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.addToBattlefield(player1, new SuntailHawk());

        Permanent hawk = findPermanent(player1, "Suntail Hawk");
        assertThat(gqs.getEffectivePower(gd, hawk)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hawk)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff non-red non-white creatures")
    void doesNotBuffOffColorCreatures() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    // ===== Red spell cast trigger: deal 3 damage to target player or planeswalker =====

    @Test
    @DisplayName("Casting a red spell deals 3 damage to the chosen player")
    void redSpellDealsDamage() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore - 3);
    }

    @Test
    @DisplayName("Casting a white spell does not fire the red damage trigger")
    void whiteSpellDoesNotDealDamage() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int p2LifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(p2LifeBefore);
    }

    // ===== White spell cast trigger: gain 3 life =====

    @Test
    @DisplayName("Casting a white spell gains 3 life")
    void whiteSpellGainsLife() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int p1LifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore + 3);
    }

    @Test
    @DisplayName("Casting a red spell does not gain life")
    void redSpellGainsNoLife() {
        harness.addToBattlefield(player1, new BalefireLiege());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);

        int p1LifeBefore = gd.getLife(player1.getId());

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(p1LifeBefore);
    }
}
