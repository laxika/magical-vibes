package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CennsHeir;
import com.github.laxika.magicalvibes.cards.o.OakgnarlWarrior;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.PloverKnights;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FavorOfTheMighty.class, CennsHeir.class, PloverKnights.class,
        OakgnarlWarrior.class, Tarfire.class, Opalescence.class})
class FavorOfTheMightyTest extends BaseCardTest {

    private void addFavor() {
        harness.addToBattlefield(player1, new FavorOfTheMighty());
    }

    private void castTarfireAt(Permanent target) {
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
    }

    @Test
    @DisplayName("Creatures tied for the greatest mana value have protection from every color")
    void greatestManaValueCreaturesHaveProtectionFromEveryColor() {
        addFavor();
        Permanent greatest = harness.addToBattlefieldAndReturn(player2, new PloverKnights());
        Permanent tiedGreatest = harness.addToBattlefieldAndReturn(player1, new PloverKnights());
        Permanent lower = harness.addToBattlefieldAndReturn(player1, new CennsHeir());

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, greatest, color)).as("greatest creature: %s", color).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, tiedGreatest, color)).as("tied creature: %s", color).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, lower, color)).as("lower creature: %s", color).isFalse();
        }
    }

    @Test
    @DisplayName("A creature with the greatest mana value cannot be targeted by a colored spell")
    void greatestManaValueCreatureIsProtectedFromColoredSpell() {
        addFavor();
        Permanent greatest = harness.addToBattlefieldAndReturn(player2, new PloverKnights());
        harness.addToBattlefield(player1, new CennsHeir());

        assertThatThrownBy(() -> castTarfireAt(greatest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A creature without the greatest mana value can be targeted by a colored spell")
    void lowerManaValueCreatureIsNotProtected() {
        addFavor();
        harness.addToBattlefield(player2, new PloverKnights());
        Permanent lower = harness.addToBattlefieldAndReturn(player1, new CennsHeir());

        castTarfireAt(lower);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Tarfire.class);
    }

    @Test
    @DisplayName("All creatures tied for greatest mana value are protected")
    void tiedGreatestManaValueAllProtected() {
        addFavor();
        harness.addToBattlefield(player2, new PloverKnights());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PloverKnights());
        harness.addToBattlefield(player1, new CennsHeir());

        assertThatThrownBy(() -> castTarfireAt(target))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection recomputes when a creature with a larger mana value appears")
    void protectionRecomputesWhenLargerCreatureAppears() {
        addFavor();
        Permanent formerlyGreatest = harness.addToBattlefieldAndReturn(player2, new PloverKnights());
        harness.addToBattlefield(player2, new OakgnarlWarrior());

        castTarfireAt(formerlyGreatest);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Battlefield view describes granted protection and its source")
    void battlefieldViewDescribesGrantedProtection() {
        addFavor();
        harness.addToBattlefield(player2, new PloverKnights());
        harness.addToBattlefield(player1, new CennsHeir());
        harness.clearMessages();

        harness.publishState();

        assertThat(harness.getConn1().getMessagesContaining(
                "\"text\":\"Protection from each color\",\"sourceName\":\"Favor of the Mighty\""))
                .isNotEmpty();
    }

    @Test
    @DisplayName("An animated Favor of the Mighty protects itself")
    void animatedFavorProtectsItself() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent favor = harness.addToBattlefieldAndReturn(player1, new FavorOfTheMighty());

        assertThat(gqs.isCreature(gd, favor)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, favor, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("A lone creature is protected even when its mana value equals Favor's")
    void loneCreatureIsProtected() {
        addFavor();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CennsHeir());

        for (CardColor color : CardColor.values()) {
            assertThat(gqs.hasProtectionFrom(gd, creature, color)).isTrue();
        }
    }

    @Test
    @DisplayName("A colored spell deals damage to a creature below the greatest mana value")
    void lowerManaValueCreatureTakesDamage() {
        addFavor();
        harness.addToBattlefield(player2, new PloverKnights());
        Permanent lower = harness.addToBattlefieldAndReturn(player1, new CennsHeir());

        castTarfireAt(lower);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cenn's Heir");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CennsHeir);
    }

    @Test
    @DisplayName("Gaining protection before resolution makes a colored spell's target illegal")
    void protectionGainedBeforeResolutionStopsColoredSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CennsHeir());
        castTarfireAt(creature);

        addFavor();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Cenn's Heir");
        assertThat(creature.getMarkedDamage()).isZero();
    }
}
