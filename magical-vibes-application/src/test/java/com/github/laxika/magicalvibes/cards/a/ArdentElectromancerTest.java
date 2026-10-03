package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrotagBugCatcher;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.k.KorCelebrant;
import com.github.laxika.magicalvibes.cards.t.TajuruParagon;
import com.github.laxika.magicalvibes.cards.z.ZulaportDuelist;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdentElectromancer.class, GrotagBugCatcher.class, IntoTheRoil.class,
        KorCelebrant.class, TajuruParagon.class, ZulaportDuelist.class})
class ArdentElectromancerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB adds one red mana for its Wizard role")
    void etbAddsManaForItsOwnPartyRole() {
        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB adds red mana equal to the size of its controller's party")
    void etbAddsManaForFullParty() {
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new ZulaportDuelist());
        harness.addToBattlefield(player1, new GrotagBugCatcher());
        harness.addToBattlefield(player1, new ArdentElectromancer());

        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void duplicateWizardsDoNotIncreasePartySize() {
        harness.addToBattlefield(player1, new ArdentElectromancer());
        harness.addToBattlefield(player1, new ArdentElectromancer());

        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void opposingCreaturesDoNotContributeToParty() {
        harness.addToBattlefield(player2, new KorCelebrant());
        harness.addToBattlefield(player2, new ZulaportDuelist());
        harness.addToBattlefield(player2, new GrotagBugCatcher());

        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void creatureWithAllPartyTypesFillsOnlyOneRole() {
        harness.addToBattlefield(player1, new TajuruParagon());

        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void flexiblePartyMemberUsesTheUnfilledRole() {
        harness.addToBattlefield(player1, new TajuruParagon());
        harness.addToBattlefield(player1, new KorCelebrant());
        harness.addToBattlefield(player1, new GrotagBugCatcher());

        castArdentElectromancer();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(4);
    }

    @Test
    void sourceCanLeaveBeforeTriggerResolvesAndEmptyPartyAddsNoMana() {
        harness.castFromHand(player1, new ArdentElectromancer(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Ardent Electromancer"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Ardent Electromancer");
        assertThat(gd.stack).hasSize(1);
        resolveStack();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    @Test
    void partyIsRecountedWhenAnotherMemberLeavesInResponse() {
        harness.addToBattlefield(player1, new GrotagBugCatcher());
        harness.castFromHand(player1, new ArdentElectromancer(), "{2}{R}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grotag Bug-Catcher"));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Grotag Bug-Catcher");
        resolveStack();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void partyIsRecountedWhenAnotherMemberEntersInResponse() {
        harness.castFromHand(player1, new ArdentElectromancer(), "{2}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, new ZulaportDuelist(), "{U}");
        resolveStack();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    private void castArdentElectromancer() {
        harness.castFromHand(player1, new ArdentElectromancer(), "{2}{R}");
        resolveStack();
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
