package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SylvokLifestaff;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodFunnel.class, GrizzlyBears.class, SylvokLifestaff.class})
class BloodFunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spells cost {2} less to cast")
    void noncreatureSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Sylvok Lifestaff"));
    }

    @Test
    @DisplayName("Sacrificing a creature lets a noncreature spell resolve")
    void sacrificingCreatureLetsSpellResolve() {
        harness.addToBattlefield(player1, new BloodFunnel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sylvok Lifestaff");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A noncreature spell is countered when its controller declines to sacrifice a creature")
    void decliningSacrificeCountersSpell() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SylvokLifestaff()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Sylvok Lifestaff");
    }

    @Test
    @DisplayName("Creature spells are neither reduced nor countered")
    void creatureSpellsAreUnaffected() {
        harness.addToBattlefield(player1, new BloodFunnel());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Grizzly Bears");
    }
}
