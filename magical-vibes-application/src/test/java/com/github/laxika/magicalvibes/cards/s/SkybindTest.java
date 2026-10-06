package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Skybind.class, GloriousAnthem.class, GrizzlyBears.class})
class SkybindTest extends BaseCardTest {

    @Test
    @DisplayName("Skybind's own entry exiles a target nonenchantment permanent")
    void ownEntryExilesTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Skybind()));
        addSkybindMana();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("An enchantment entering under your control triggers Skybind")
    void allyEnchantmentEntryExilesTarget() {
        harness.addToBattlefield(player1, new Skybind());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiled permanent returns at the beginning of the next end step")
    void exiledPermanentReturnsAtEndStep() {
        harness.addToBattlefield(player1, new Skybind());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger Skybind")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new Skybind());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.forceActivePlayer(player2);
        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Skybind cannot target an enchantment")
    void cannotTargetEnchantment() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        harness.setHand(player1, List.of(new Skybind()));
        addSkybindMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Constellation excludes enchantments from its target choices")
    void allyTriggerOnlyOffersNonenchantments() {
        harness.addToBattlefield(player1, new Skybind());
        UUID enchantmentId = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem()).getId();
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validPermanentIds()).containsExactly(creatureId).doesNotContain(enchantmentId);
        harness.handlePermanentChosen(player1, creatureId);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
    }

    @Test
    @DisplayName("Constellation with no nonenchantment permanent has no legal target")
    void allyTriggerWithNoLegalTarget() {
        harness.addToBattlefield(player1, new Skybind());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Skybind");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("An exiled stolen permanent returns to its owner as a new untapped permanent")
    void stolenPermanentReturnsToOwner() {
        harness.addToBattlefield(player1, new Skybind());
        UUID creatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        gd.stolenCreatures.put(creatureId, player2.getId());
        findPermanent(player1, "Grizzly Bears").tap();
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creatureId);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player2, "Grizzly Bears").getId()).isNotEqualTo(creatureId);
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isFalse();
    }

    private void addSkybindMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
