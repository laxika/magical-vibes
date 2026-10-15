package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.k.KarganIntimidator;
import com.github.laxika.magicalvibes.cards.z.ZulaportDuelist;
import com.github.laxika.magicalvibes.cards.a.ArdentElectromancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.e.ExpeditionHealer;
import com.github.laxika.magicalvibes.cards.t.TajuruParagon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpoilsOfAdventure.class, KarganIntimidator.class, ZulaportDuelist.class, Forest.class,
        ArdentElectromancer.class, ExpeditionHealer.class, TajuruParagon.class})
class SpoilsOfAdventureTest extends BaseCardTest {

    @Test
    @DisplayName("Costs four less with a full party")
    void costsFourLessWithFullParty() {
        addFullParty(player1);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Gains three life and draws three cards")
    void gainsLifeAndDrawsCards() {
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).allMatch(card -> card instanceof Forest);
    }

    @Test
    @DisplayName("Does not count an opponent's party")
    void doesNotCountOpponentsParty() {
        addFullParty(player2);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two Clerics fill only one party role")
    void duplicateRolesReduceCostOnlyOnce() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A creature with all party types fills only one role")
    void oneMultitypeCreatureReducesCostByOne() {
        harness.addToBattlefield(player1, new TajuruParagon());
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A multitype creature fills the missing role in the largest party")
    void multitypeCreatureFillsMissingRole() {
        harness.addToBattlefield(player1, new TajuruParagon());
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ZulaportDuelist());
        harness.addToBattlefield(player1, new KarganIntimidator());
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A full party does not remove the blue mana requirement")
    void fullPartyStillRequiresColoredMana() {
        addFullParty(player1);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Two different party roles reduce the cost by two")
    void partialPartyReducesCostByTwo() {
        harness.addToBattlefield(player1, new ExpeditionHealer());
        harness.addToBattlefield(player1, new ZulaportDuelist());
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Losing the party after casting does not change the spell's effects")
    void losingPartyAfterCastingDoesNotChangeResolution() {
        addFullParty(player1);
        harness.setHand(player1, List.of(new SpoilsOfAdventure()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertLife(player2, 20);
    }
    private void addFullParty(com.github.laxika.magicalvibes.model.Player player) {
        harness.addToBattlefield(player, new ExpeditionHealer());
        harness.addToBattlefield(player, new ZulaportDuelist());
        harness.addToBattlefield(player, new KarganIntimidator());
        harness.addToBattlefield(player, new ArdentElectromancer());
    }
}
