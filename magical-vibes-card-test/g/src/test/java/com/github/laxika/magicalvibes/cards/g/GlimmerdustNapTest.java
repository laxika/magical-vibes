package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GlimmerdustNap.class, GoldmeadowDodger.class})
class GlimmerdustNapTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a tapped creature")
    void canTargetTappedCreature() {
        Permanent bears = addCreatureReady(player2, new GoldmeadowDodger());
        bears.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent untapped = addCreatureReady(player2, new GoldmeadowDodger());

        // A legal tapped target exists too, so the card is playable and the
        // rejection reports the target restriction rather than "not playable".
        Permanent tapped = addCreatureReady(player2, new GoldmeadowDodger());
        tapped.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, untapped.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    @DisplayName("Fizzles if the target becomes untapped before resolution")
    void fizzlesIfTargetBecomesUntappedBeforeResolution() {
        Permanent creature = addCreatureReady(player2, new GoldmeadowDodger());
        creature.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());

        creature.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Glimmerdust Nap");
        harness.assertInGraveyard(player1, "Glimmerdust Nap");
    }

    @Test
    @DisplayName("Glimmerdust Nap goes to the graveyard if its creature becomes untapped")
    void auraLeavesWhenEnchantedCreatureBecomesUntapped() {
        Permanent creature = addCreatureReady(player2, new GoldmeadowDodger());
        creature.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        creature.untap();
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Glimmerdust Nap");
        harness.assertInGraveyard(player1, "Glimmerdust Nap");
    }

    @Test
    @DisplayName("Resolving attaches Glimmerdust Nap to the tapped creature")
    void resolvingAttachesToTarget() {
        Permanent bears = addCreatureReady(player2, new GoldmeadowDodger());
        bears.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Glimmerdust Nap")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = addCreatureReady(player2, new GoldmeadowDodger());
        bears.tap();

        Permanent nap = harness.addToBattlefieldAndReturn(player1, new GlimmerdustNap());
        nap.setAttachedTo(bears.getId());

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again after Glimmerdust Nap is removed")
    void creatureUntapsAfterRemoval() {
        Permanent bears = addCreatureReady(player2, new GoldmeadowDodger());
        bears.tap();

        Permanent nap = harness.addToBattlefieldAndReturn(player1, new GlimmerdustNap());
        nap.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(nap);

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can enchant your own tapped creature without preventing other creatures from untapping")
    void onlyEnchantedCreatureStaysTapped() {
        Permanent enchanted = addCreatureReady(player1, new GoldmeadowDodger());
        enchanted.tap();
        Permanent other = addCreatureReady(player1, new GoldmeadowDodger());
        other.tap();

        harness.setHand(player1, List.of(new GlimmerdustNap()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, enchanted.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Glimmerdust Nap");
        advanceToUpkeep(player1);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Glimmerdust Nap");
    }

}
