package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.ChildOfAlara;
import com.github.laxika.magicalvibes.cards.f.FallajiWayfarer;
import com.github.laxika.magicalvibes.cards.f.FusionElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ManaCannons.class, Terminate.class, ChildOfAlara.class, GrizzlyBears.class, FallajiWayfarer.class, FusionElemental.class, MycosynthLattice.class, SolRing.class})
class ManaCannonsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of colors in a two-color spell")
    void dealsDamageForTwoColorSpell() {
        harness.addToBattlefield(player1, new ManaCannons());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, victim.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage equal to the number of colors in a five-color spell")
    void dealsDamageForFiveColorSpell() {
        harness.addToBattlefield(player1, new ManaCannons());
        harness.setHand(player1, List.of(new ChildOfAlara()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Does not trigger for a monocolored spell")
    void doesNotTriggerForMonocoloredSpell() {
        harness.addToBattlefield(player1, new ManaCannons());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void dealsFiveDamageForAllColorsAbilityDespiteSingleColorManaCost() {
        harness.addToBattlefield(player1, new ManaCannons());
        harness.setHand(player1, List.of(new FallajiWayfarer()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 15);
    }

    @Test
    void doesNotTriggerWhenLatticeMakesMulticoloredSpellColorless() {
        harness.addToBattlefield(player1, new ManaCannons());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new FusionElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Fusion Elemental");
    }

    @Test
    void canDamageCreatureBeforeTriggeringSpellResolves() {
        harness.addToBattlefield(player1, new ManaCannons());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new FallajiWayfarer());
        harness.setHand(player1, List.of(new FusionElemental()));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fallaji Wayfarer");
        harness.assertNotOnBattlefield(player1, "Fusion Elemental");
        harness.assertLife(player2, 20);
    }

    @Test
    void doesNotTriggerForColorlessSpell() {
        harness.addToBattlefield(player1, new ManaCannons());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void doesNotTriggerForOpponentsMulticoloredSpell() {
        harness.addToBattlefield(player1, new ManaCannons());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new FallajiWayfarer());
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, victim.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Fallaji Wayfarer");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
