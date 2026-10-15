package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CliffsideLookout;
import com.github.laxika.magicalvibes.cards.e.EldraziSkyspawner;
import com.github.laxika.magicalvibes.cards.h.HedronArchive;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoltenNursery.class, Ornithopter.class, GrizzlyBears.class,
        CliffsideLookout.class, EldraziSkyspawner.class, HedronArchive.class, MycosynthLattice.class})
class MoltenNurseryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a colorless spell triggers 1 damage to any target")
    void colorlessSpellDealsDamageToTarget() {
        harness.addToBattlefield(player1, new MoltenNursery());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a colored spell does not trigger")
    void coloredSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoltenNursery());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void devoidSpellTriggersBeforeItResolves() {
        harness.addToBattlefield(player1, new MoltenNursery());
        harness.setHand(player1, List.of(new EldraziSkyspawner()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Eldrazi Skyspawner");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void colorlessNoncreatureSpellCanDamageCreature() {
        harness.addToBattlefield(player1, new MoltenNursery());
        var target = harness.addToBattlefieldAndReturn(player2, new CliffsideLookout());
        harness.setHand(player1, List.of(new HedronArchive()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cliffside Lookout");
        harness.assertNotOnBattlefield(player2, "Cliffside Lookout");
        harness.assertNotOnBattlefield(player1, "Hedron Archive");
    }

    @Test
    void opponentColorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new MoltenNursery());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HedronArchive()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.setLife(player1, 20);

        harness.castArtifact(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player2, "Hedron Archive");
    }

    @Test
    void castingNurseryDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new MoltenNursery()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Molten Nursery");
    }

    @Test
    void latticeMakesNormallyColoredSpellTriggerNursery() {
        harness.addToBattlefield(player1, new MoltenNursery());
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new CliffsideLookout()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player1, "Cliffside Lookout");
    }
}
