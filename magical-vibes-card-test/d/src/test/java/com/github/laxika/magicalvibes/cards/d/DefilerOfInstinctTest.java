package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Omniscience;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WarTorchGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Defiler of Instinct")
@CardUsed({DefilerOfInstinct.class, WarTorchGoblin.class, Shock.class, GrizzlyBears.class, Omniscience.class})
class DefilerOfInstinctTest extends BaseCardTest {

    @Test
    @DisplayName("paying 2 life reduces a red permanent spell by {R} and deals 1 damage")
    void paysLifeForRedPermanentSpell() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new WarTorchGoblin()));
        harness.setLife(player1, 20);

        castCreaturePayingLife();
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("declining the life payment pays the full mana cost and deals 1 damage")
    void paysReducedManaForRedPermanentSpell() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new WarTorchGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("red nonpermanent and nonred permanent spells do not trigger")
    void ignoresNonMatchingSpells() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @CardUsed({DefilerOfInstinct.class})
    @DisplayName("two Defilers do not reduce costs when neither life payment is chosen")
    void multipleDefilersWithoutLifePaymentPayFullCost() {
        addCreatureReady(player1, new DefilerOfInstinct());
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new DefilerOfInstinct()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed({DefilerOfInstinct.class})
    @DisplayName("paying 2 life with two Defilers only replaces one red mana")
    void multipleDefilersNeedSeparateLifePaymentsForEachReduction() {
        addCreatureReady(player1, new DefilerOfInstinct());
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new DefilerOfInstinct()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        castCreaturePayingLife();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @CardUsed({DefilerOfInstinct.class})
    @DisplayName("life payment cannot replace a second red mana symbol")
    void singleDefilerCannotReplaceBothRedSymbols() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.setHand(player1, List.of(new DefilerOfInstinct()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(this::castCreaturePayingLife).isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({DefilerOfInstinct.class})
    @DisplayName("an opponent's red permanent spell receives neither reduction nor damage trigger")
    void opponentsSpellIsUnaffected() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new DefilerOfInstinct()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player2, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.assertOnBattlefield(player2, "Defiler of Instinct");
    }

    @Test
    @CardUsed({DefilerOfInstinct.class, Omniscience.class})
    @DisplayName("a free red permanent spell does not require paying life or red mana")
    void freeSpellCanDeclineLifePayment() {
        addCreatureReady(player1, new DefilerOfInstinct());
        harness.addToBattlefield(player1, new Omniscience());
        harness.setHand(player1, List.of(new DefilerOfInstinct()));

        harness.castCreature(player1, 0);
        chooseTriggerTarget(player2.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof DefilerOfInstinct).hasSize(2);
    }

    @Test
    @DisplayName("the cast trigger can kill a creature before the permanent spell resolves")
    void castTriggerCanTargetCreature() {
        addCreatureReady(player1, new DefilerOfInstinct());
        var target = addCreatureReady(player2, new WarTorchGoblin());
        harness.setHand(player1, List.of(new WarTorchGoblin()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "War-Torch Goblin");
        harness.assertNotOnBattlefield(player1, "War-Torch Goblin");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "War-Torch Goblin");
    }

    private void castCreaturePayingLife() {
        harness.castInstantWithLifeOrManaAdditionalCost(player1, 0, null, true);
    }

    private void chooseTriggerTarget(java.util.UUID targetId) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
