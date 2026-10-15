package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeaGateColossus.class, BoggartBrute.class, FaerieMiscreant.class,
        FugitiveWizard.class, SoulWarden.class, StoneworkPackbeast.class})
class SeaGateColossusTest extends BaseCardTest {

    @Test
    @DisplayName("A full party reduces the generic cost by four")
    void fullPartyReducesCostByFour() {
        addFullParty();
        harness.castFromHand(player1, new SeaGateColossus(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Without a party, the full generic cost is required")
    void withoutPartyRequiresFullGenericCost() {
        harness.setHand(player1, List.of(new SeaGateColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A partial party reduces the generic cost by its party size")
    void partialPartyReducesCostByPartySize() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.castFromHand(player1, new SeaGateColossus(), "{4}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("With no party, seven mana pays the full cost")
    void withoutPartyCanCastForSeven() {
        harness.castFromHand(player1, new SeaGateColossus(), "{7}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A full party still requires three mana")
    void fullPartyCannotCastForTwo() {
        addFullParty();
        harness.setHand(player1, List.of(new SeaGateColossus()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Warriors count as only one party member")
    void duplicateRolesDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new SeaGateColossus());
        harness.addToBattlefield(player1, new SeaGateColossus());

        harness.castFromHand(player1, new SeaGateColossus(), "{6}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("One creature with all four party types counts only once")
    void multiRoleCreatureCountsOnce() {
        harness.addToBattlefield(player1, new StoneworkPackbeast());

        harness.castFromHand(player1, new SeaGateColossus(), "{6}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Four creatures with all party types form a full party")
    void multiRoleCreaturesFillDistinctRoles() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }

        harness.castFromHand(player1, new SeaGateColossus(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("More than four eligible creatures cannot reduce the cost further")
    void partyReductionIsCappedAtFour() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new StoneworkPackbeast());
        }

        harness.castFromHand(player1, new SeaGateColossus(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's party does not reduce the caster's cost")
    void opponentsPartyDoesNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new StoneworkPackbeast());
        }

        harness.castFromHand(player1, new SeaGateColossus(), "{7}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Party creatures in the graveyard do not reduce the cost")
    void graveyardCreaturesDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new StoneworkPackbeast(),
                new StoneworkPackbeast(), new StoneworkPackbeast(), new StoneworkPackbeast()));

        harness.castFromHand(player1, new SeaGateColossus(), "{7}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private void addFullParty() {
        harness.addToBattlefield(player1, new SoulWarden());
        harness.addToBattlefield(player1, new FaerieMiscreant());
        harness.addToBattlefield(player1, new BoggartBrute());
        harness.addToBattlefield(player1, new FugitiveWizard());
    }
}
