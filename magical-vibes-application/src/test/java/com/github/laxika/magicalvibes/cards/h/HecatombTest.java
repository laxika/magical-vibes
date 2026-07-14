package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HecatombTest extends BaseCardTest {

    private long creaturesControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .count();
    }

    private void castHecatomb() {
        harness.setHand(player1, List.of(new Hecatomb()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // resolve enchantment spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB
    }

    @Test
    @DisplayName("Tapping a Swamp deals 1 damage to a target player")
    void dealsDamageToPlayer() {
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // The lone Swamp is tapped as a cost.
        assertThat(findPermanent(player1, "Swamp").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping a Swamp deals 1 damage to a target creature")
    void dealsDamageToCreature() {
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elvesId = findPermanent(player2, "Llanowar Elves").getId();

        harness.activateAbility(player1, 0, null, elvesId);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Cannot activate the ability without an untapped Swamp")
    void cannotActivateWithoutUntappedSwamp() {
        harness.addToBattlefield(player1, new Hecatomb());
        Permanent swamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        swamp.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("With multiple Swamps the controller chooses which to tap")
    void multipleSwampsPromptChoice() {
        harness.addToBattlefield(player1, new Hecatomb());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB auto-sacrifices Hecatomb when controller has fewer than four creatures")
    void etbAutoSacrificesWithoutFourCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hecatomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hecatomb"));
        // The three creatures are untouched.
        assertThat(creaturesControlledBy(player1.getId())).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB accepting with exactly four creatures sacrifices all four and keeps Hecatomb")
    void etbAcceptWithFourCreatures() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(creaturesControlledBy(player1.getId())).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hecatomb"));
    }

    @Test
    @DisplayName("ETB declining sacrifices Hecatomb and keeps the creatures")
    void etbDeclineSacrificesHecatomb() {
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player1, new LlanowarElves());
        castHecatomb();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hecatomb"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hecatomb"));
        assertThat(creaturesControlledBy(player1.getId())).isEqualTo(4);
    }
}
