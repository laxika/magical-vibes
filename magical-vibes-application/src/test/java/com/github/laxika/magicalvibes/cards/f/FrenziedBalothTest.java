package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FrenziedBaloth.class, Cancel.class, GrizzlyBears.class, Shock.class})
class FrenziedBalothTest extends BaseCardTest {

    @Test
    @DisplayName("Frenzied Baloth can't be countered")
    void cannotBeCountered() {
        FrenziedBaloth baloth = new FrenziedBaloth();
        harness.setHand(player1, List.of(baloth));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, baloth.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frenzied Baloth");
        harness.assertNotInGraveyard(player1, "Frenzied Baloth");
    }

    @Test
    @DisplayName("Frenzied Baloth makes your creature spells unable to be countered")
    void protectsCreatureSpellsYouControl() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Frenzied Baloth prevents combat damage prevention from an opposing creature")
    void combatDamageCannotBePrevented() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        gd.playerDamagePreventionShields.put(player1.getId(), 5);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of());
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
        assertThat(gd.playerDamagePreventionShields).containsEntry(player1.getId(), 5);
    }

    @Test
    @DisplayName("Frenzied Baloth does not prevent noncombat damage prevention")
    void noncombatDamageCanBePrevented() {
        harness.addToBattlefield(player1, new FrenziedBaloth());
        gd.playerDamagePreventionShields.put(player1.getId(), 2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.playerDamagePreventionShields).isEmpty();
    }
}
