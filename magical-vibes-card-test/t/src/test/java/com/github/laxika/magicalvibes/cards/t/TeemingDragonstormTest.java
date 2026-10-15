package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MaskwoodNexus;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeemingDragonstorm.class, GrizzlyBears.class, ShivanDragon.class, MaskwoodNexus.class, Opalescence.class})
class TeemingDragonstormTest extends BaseCardTest {

    @Test
    @DisplayName("Entering creates two 2/2 white Soldier tokens")
    void enteringCreatesSoldierTokens() {
        harness.setHand(player1, List.of(new TeemingDragonstorm()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> soldiers = battlefield.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .toList();

        assertThat(soldiers).hasSize(2);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getPower()).isEqualTo(2);
            assertThat(soldier.getCard().getToughness()).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Returns to its owner's hand when a Dragon you control enters")
    void returnsWhenAllyDragonEnters() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teeming Dragonstorm");
        harness.assertInHand(player1, "Teeming Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when a non-Dragon creature enters")
    void doesNotReturnForNonDragon() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
    }

    @Test
    @DisplayName("Does not return when an opponent's Dragon enters")
    void doesNotReturnForOpponentDragon() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ShivanDragon()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
    }

    @Test
    @DisplayName("Returns to the owner rather than the controller")
    void returnsToOwnerHand() {
        TeemingDragonstorm enchantment = new TeemingDragonstorm();
        enchantment.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, enchantment);
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teeming Dragonstorm");
        harness.assertInHand(player2, "Teeming Dragonstorm");
        harness.assertNotInHand(player1, "Teeming Dragonstorm");
        harness.assertOnBattlefield(player1, "Shivan Dragon");
    }

    @Test
    @DisplayName("A Dragon entering returns each Teeming Dragonstorm")
    void returnsEachEnchantmentIndependently() {
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.addToBattlefield(player1, new TeemingDragonstorm());
        harness.setHand(player1, List.of(new ShivanDragon()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Teeming Dragonstorm");
        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card instanceof TeemingDragonstorm)
                .hasSize(2);
        harness.assertOnBattlefield(player1, "Shivan Dragon");
    }

    @Test
    @DisplayName("Replaying the returned enchantment creates two more Soldiers")
    void replayCreatesAdditionalSoldiers() {
        harness.setHand(player1, List.of(new TeemingDragonstorm(), new ShivanDragon()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Teeming Dragonstorm");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);

        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
        harness.assertOnBattlefield(player1, "Shivan Dragon");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .hasSize(4);
    }

    @Test
    @DisplayName("Entering as a Dragon triggers both of the enchantment's abilities")
    void triggersItsOwnReturnWhenEnteringAsDragon() {
        harness.addToBattlefield(player1, new Opalescence());
        harness.addToBattlefield(player1, new MaskwoodNexus());
        harness.setHand(player1, List.of(new TeemingDragonstorm()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Teeming Dragonstorm");
        assertThat(gd.stack).hasSize(2);
    }
}
