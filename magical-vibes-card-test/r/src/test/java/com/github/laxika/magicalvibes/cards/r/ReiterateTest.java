package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.Conflagrate;
import com.github.laxika.magicalvibes.cards.o.OrcishCannonade;
import com.github.laxika.magicalvibes.cards.r.RiftBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reiterate.class, RiftBolt.class, BenalishCavalry.class, OrcishCannonade.class,
        Cancel.class, Conflagrate.class})
class ReiterateTest extends BaseCardTest {

    @Test
    @DisplayName("Copies a target instant or sorcery spell")
    void copiesTargetInstantOrSorcerySpell() {
        RiftBolt riftBolt = new RiftBolt();
        harness.setHand(player1, List.of(riftBolt, new Reiterate()));
        addReiterateMana(false);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, riftBolt.getId());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
        harness.assertInGraveyard(player1, "Reiterate");
    }

    @Test
    @DisplayName("Buyback returns Reiterate to its owner's hand when it resolves")
    void buybackReturnsToHand() {
        RiftBolt riftBolt = new RiftBolt();
        harness.setHand(player1, List.of(riftBolt, new Reiterate()));
        addReiterateMana(true);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstantWithBuyback(player1, 0, riftBolt.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Reiterate");
        harness.assertNotInGraveyard(player1, "Reiterate");
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        BenalishCavalry cavalry = new BenalishCavalry();
        harness.setHand(player1, List.of(cavalry, new Reiterate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, cavalry.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A copy may choose a new target and resolve independently")
    void copyMayChooseNewTarget() {
        OrcishCannonade cannonade = new OrcishCannonade();
        harness.setHand(player1, List.of(cannonade, new Reiterate()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.castAndResolveInstant(player1, 0, cannonade.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());

        StackEntry copy = gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow();
        assertThat(copy.getTargetId()).isEqualTo(player1.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Buyback does not return Reiterate when its only target is countered")
    void buybackDoesNotReturnWithIllegalTarget() {
        RiftBolt riftBolt = new RiftBolt();
        harness.setHand(player1, List.of(riftBolt, new Reiterate(), new Cancel()));
        addReiterateMana(true);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.castInstantWithBuyback(player1, 0, riftBolt.getId());
        harness.castAndResolveInstant(player1, 0, riftBolt.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reiterate");
        harness.assertNotInHand(player1, "Reiterate");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May change more than one target of a copied spell")
    void mayChangeMultipleCopyTargets() {
        Conflagrate conflagrate = new Conflagrate();
        harness.setHand(player1, List.of(conflagrate, new Reiterate()));
        harness.addToBattlefield(player1, new BenalishCavalry());
        UUID cavalryId = harness.getPermanentId(player1, "Benalish Cavalry");
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        assignments.put(player1.getId(), 1);
        assignments.put(player2.getId(), 1);

        harness.castSorceryForX(player1, 0, 2, assignments);
        harness.castAndResolveInstant(player1, 0, conflagrate.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, cavalryId);

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Benalish Cavalry");
    }

    private void addReiterateMana(boolean buyback) {
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, buyback ? 6 : 3);
    }
}
