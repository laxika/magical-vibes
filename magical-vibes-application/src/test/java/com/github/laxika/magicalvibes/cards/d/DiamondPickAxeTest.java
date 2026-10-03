package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.c.Colossadactyl;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiamondPickAxe.class, Colossadactyl.class, Abrade.class})
class DiamondPickAxeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new Colossadactyl());
        Permanent pickAxe = addPickAxeReady(player1);
        pickAxe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attacking with the equipped creature creates a Treasure token")
    void attackCreatesTreasureToken() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new Colossadactyl());
        pickAxe.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("An unattached Diamond Pick-Axe does not create a Treasure when a creature attacks")
    void unattachedPickAxeDoesNotTrigger() {
        addPickAxeReady(player1);
        addCreatureReady(player1, new Colossadactyl());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Equip {2} attaches Diamond Pick-Axe to a creature you control")
    void equipAttachesToCreature() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new Colossadactyl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(pickAxe.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The equipped creature's controller creates the Treasure even if the Equipment has another controller")
    void creatureControllerCreatesTreasure() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player2, new Colossadactyl());
        pickAxe.setAttachedTo(creature.getId());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player2, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Attacking with a different creature does not create a Treasure")
    void otherCreatureDoesNotTrigger() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent equipped = addCreatureReady(player1, new Colossadactyl());
        addCreatureReady(player1, new Colossadactyl());
        pickAxe.setAttachedTo(equipped.getId());

        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("An attack trigger still creates its Treasure after the Equipment becomes unattached")
    void detachingDoesNotRemovePendingTrigger() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new Colossadactyl());
        pickAxe.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        pickAxe.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Diamond Pick-Axe survives an effect that destroys an artifact")
    void survivesArtifactDestruction() {
        Permanent pickAxe = addPickAxeReady(player2);
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, 1, pickAxe.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Diamond Pick-Axe");
        harness.assertNotInGraveyard(player2, "Diamond Pick-Axe");
        harness.assertInGraveyard(player1, "Abrade");
    }

    @Test
    @DisplayName("Re-equipping moves the boost and attack trigger to the new creature")
    void reequippingMovesGrantedAbilities() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent first = addCreatureReady(player1, new Colossadactyl());
        Permanent second = addCreatureReady(player1, new Colossadactyl());
        pickAxe.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(pickAxe.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);

        declareAttackers(player1, List.of(1, 2));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpposingCreature() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player2, new Colossadactyl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pickAxe.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires the full two mana")
    void cannotEquipWithInsufficientMana() {
        Permanent pickAxe = addPickAxeReady(player1);
        Permanent creature = addCreatureReady(player1, new Colossadactyl());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(pickAxe.getAttachedTo()).isNull();
    }

    private Permanent addPickAxeReady(com.github.laxika.magicalvibes.model.Player player) {
        return harness.addToBattlefieldAndReturn(player, new DiamondPickAxe());
    }
}
