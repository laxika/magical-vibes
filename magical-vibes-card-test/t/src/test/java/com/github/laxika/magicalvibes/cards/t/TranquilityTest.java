package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.a.ArmadilloCloak;
import com.github.laxika.magicalvibes.cards.d.DuelingGrounds;
import com.github.laxika.magicalvibes.cards.e.EyeOfRamos;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LlanowarVanguard;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Tranquility.class, DuelingGrounds.class, ArmadilloCloak.class, LlanowarVanguard.class,
        Forest.class, EyeOfRamos.class})
class TranquilityTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys enchantments controlled by both players")
    void destroysEnchantmentsFromBothPlayers() {
        harness.addToBattlefield(player1, new DuelingGrounds());
        harness.addToBattlefield(player2, new DuelingGrounds());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Dueling Grounds");
        harness.assertNotOnBattlefield(player2, "Dueling Grounds");
        harness.assertInGraveyard(player1, "Dueling Grounds");
        harness.assertInGraveyard(player2, "Dueling Grounds");
    }

    @Test
    @DisplayName("Destroys auras attached to creatures but not the creatures")
    void destroysAurasButNotCreatures() {
        Permanent vanguard = addCreatureReady(player1, new LlanowarVanguard());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new ArmadilloCloak());
        auraPerm.setAttachedTo(vanguard.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armadillo Cloak");
        harness.assertOnBattlefield(player1, "Llanowar Vanguard");
        harness.assertInGraveyard(player1, "Armadillo Cloak");
    }

    @Test
    @DisplayName("Does not destroy creatures or lands")
    void doesNotDestroyCreaturesOrLands() {
        harness.addToBattlefield(player1, new LlanowarVanguard());
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Vanguard");
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Does not destroy a non-enchantment artifact")
    void doesNotDestroyArtifact() {
        harness.addToBattlefield(player2, new EyeOfRamos());
        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Eye of Ramos");
    }

    @Test
    @DisplayName("Destroys every enchantment when one player controls several")
    void destroysMultipleEnchantmentsOnOneBattlefield() {
        harness.addToBattlefield(player2, new DuelingGrounds());
        harness.addToBattlefield(player2, new DuelingGrounds());
        harness.addToBattlefield(player2, new DuelingGrounds());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dueling Grounds");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Dueling Grounds"))
                .hasSize(3);
        harness.assertInGraveyard(player1, "Tranquility");
    }

    @Test
    @DisplayName("Destroys an aura attached to an opponent's creature")
    void destroysAuraAttachedToOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new LlanowarVanguard());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ArmadilloCloak());
        aura.setAttachedTo(creature.getId());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armadillo Cloak");
        harness.assertInGraveyard(player1, "Armadillo Cloak");
        harness.assertNotInGraveyard(player2, "Armadillo Cloak");
        harness.assertOnBattlefield(player2, "Llanowar Vanguard");
    }

    @Test
    @DisplayName("Leaves enchantment cards in hand and graveyard untouched")
    void leavesEnchantmentCardsOutsideBattlefieldUntouched() {
        DuelingGrounds inHand = new DuelingGrounds();
        DuelingGrounds inGraveyard = new DuelingGrounds();
        harness.setHand(player2, List.of(inHand));
        harness.setGraveyard(player2, List.of(inGraveyard));
        harness.addToBattlefield(player2, new DuelingGrounds());

        harness.castFromHand(player1, new Tranquility(), "{2}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(inHand);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(inGraveyard)
                .hasSize(2);
        harness.assertNotOnBattlefield(player2, "Dueling Grounds");
    }
}
