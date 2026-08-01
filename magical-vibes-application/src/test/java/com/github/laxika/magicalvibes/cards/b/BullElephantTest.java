package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BullElephantTest extends BaseCardTest {

    private long forestsControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.FOREST))
                .count();
    }

    private void castBullElephant() {
        harness.setHand(player1, List.of(new BullElephant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has fewer than two Forests")
    void autoSacrificesWithoutTwoForests() {
        harness.addToBattlefield(player1, new Forest());
        castBullElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Bull Elephant");
        harness.assertInGraveyard(player1, "Bull Elephant");
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Forest lands do not count toward the return cost")
    void nonForestLandsDoNotCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        castBullElephant();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Bull Elephant");
        harness.assertInGraveyard(player1, "Bull Elephant");
    }

    @Test
    @DisplayName("Prompts a may ability when controller has two or more Forests")
    void promptsMayAbilityWithTwoForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castBullElephant();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting with exactly two Forests returns them and keeps Bull Elephant")
    void acceptWithExactlyTwoForests() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castBullElephant();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(c -> c.getSubtypes().contains(CardSubtype.FOREST)).count()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Bull Elephant");
    }

    @Test
    @DisplayName("Accepting with three Forests lets controller choose which two to return")
    void acceptWithThreeForestsChoosesTwo() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castBullElephant();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);

        List<UUID> forestIds = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.FOREST))
                .map(Permanent::getId)
                .limit(2)
                .toList();
        harness.handleMultiplePermanentsChosen(player1, forestIds);

        assertThat(forestsControlledBy(player1.getId())).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Bull Elephant");
    }

    @Test
    @DisplayName("Declining sacrifices Bull Elephant and keeps the Forests")
    void declineSacrificesBullElephant() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castBullElephant();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Bull Elephant");
        harness.assertInGraveyard(player1, "Bull Elephant");
        assertThat(forestsControlledBy(player1.getId())).isEqualTo(2);
    }
}
