package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MockeryOfNature.class, AngelicChorus.class, FountainOfYouth.class, GrizzlyBears.class})
class MockeryOfNatureTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, may destroy a target artifact")
    void castTriggerDestroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        castMockery();

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Fountain of Youth");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mockery of Nature");
    }

    @Test
    @DisplayName("When cast, may destroy a target enchantment")
    void castTriggerDestroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        castMockery();

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves the target on the battlefield")
    void decliningCastTriggerDoesNotDestroyTarget() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        castMockery();

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The cast trigger cannot target a creature")
    void castTriggerCannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        castMockery();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Emerge sacrifices a creature and reduces the generic cost by its mana value")
    void emergeSacrificesCreatureAndReducesCost() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID sacrificedId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.setHand(player1, List.of(new MockeryOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mockery of Nature");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Casting with no legal target still resolves the creature")
    void castingWithoutLegalTargetsResolvesCreature() {
        castMockery();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mockery of Nature");
    }

    @Test
    @DisplayName("Entering without being cast does not destroy an artifact")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.enterBattlefieldAndReturn(player1, new MockeryOfNature());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("The cast trigger can destroy its controller's artifact before the creature resolves")
    void castTriggerCanDestroyOwnArtifact() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        UUID targetId = harness.getPermanentId(player1, "Fountain of Youth");
        castMockery();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Fountain of Youth");
        harness.assertNotOnBattlefield(player1, "Mockery of Nature");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mockery of Nature");
    }

    @Test
    @DisplayName("Emerge reduction greater than the generic cost leaves the green cost payable")
    void emergeReductionIsCappedAtGenericCost() {
        harness.addToBattlefield(player1, new MockeryOfNature());
        UUID sacrificedId = harness.getPermanentId(player1, "Mockery of Nature");
        harness.setHand(player1, List.of(new MockeryOfNature()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId));
        harness.assertInGraveyard(player1, "Mockery of Nature");
        harness.assertNotOnBattlefield(player1, "Mockery of Nature");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mockery of Nature");
    }

    @Test
    @DisplayName("Emerge cannot replace the required green mana with generic mana")
    void emergeStillRequiresGreenMana() {
        harness.addToBattlefield(player1, new MockeryOfNature());
        UUID sacrificedId = harness.getPermanentId(player1, "Mockery of Nature");
        harness.setHand(player1, List.of(new MockeryOfNature()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of(sacrificedId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Mockery of Nature");
        harness.assertNotInGraveyard(player1, "Mockery of Nature");
        harness.assertInHand(player1, "Mockery of Nature");
    }

    private void castMockery() {
        harness.setHand(player1, List.of(new MockeryOfNature()));
        harness.addMana(player1, ManaColor.COLORLESS, 9);
        harness.castCreature(player1, 0);
    }
}
