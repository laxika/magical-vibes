package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CavalierOfDawn.class, DoomBlade.class, Forest.class, RuneclawBear.class,
        Pacifism.class, Spellbook.class})
class CavalierOfDawnTest extends BaseCardTest {

    @Test
    @DisplayName("ETB destroys a nonland permanent and gives its controller a Golem")
    void etbDestroysNonlandPermanentAndCreatesGolem() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castCavalier(List.of(target.getId()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(findPermanents(player2, "Golem")).singleElement().satisfies(golem -> {
            assertThat(golem.getCard().getPower()).isEqualTo(3);
            assertThat(golem.getCard().getToughness()).isEqualTo(3);
            assertThat(golem.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(golem.getCard().hasType(CardType.CREATURE)).isTrue();
        });
    }

    @Test
    @DisplayName("ETB can resolve without choosing a target")
    void etbCanResolveWithoutTarget() {
        harness.castFromHand(player1, new CavalierOfDawn(), "{2}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Cavalier of Dawn");
        assertThat(findPermanents(player1, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new CavalierOfDawn()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland permanent");
    }

    @Test
    @DisplayName("Death trigger returns an artifact or enchantment card to hand")
    void deathReturnsArtifactOrEnchantmentToHand() {
        Card artifact = new Spellbook();
        Card enchantment = new Pacifism();
        Card creature = new RuneclawBear();
        harness.setGraveyard(player1, List.of(artifact, enchantment, creature));

        killCavalier();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());

        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Runeclaw Bear");
    }

    @Test
    @DisplayName("Death trigger has no choice when no artifact or enchantment card is in the graveyard")
    void deathHasNoChoiceWithoutEligibleCard() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));

        killCavalier();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB can destroy your own artifact and gives you the Golem")
    void etbDestroysOwnArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        castCavalier(List.of(target.getId()));

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(findPermanents(player1, "Golem")).hasSize(1);
        assertThat(findPermanents(player2, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("An ETB target removed in response produces no Golem")
    void etbWithRemovedTargetCreatesNoGolem() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        castCavalier(List.of(target.getId()));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Runeclaw Bear");
        assertThat(findPermanents(player1, "Golem")).isEmpty();
        assertThat(findPermanents(player2, "Golem")).isEmpty();
    }

    @Test
    @DisplayName("Death trigger returns an enchantment and cannot choose the opponent's artifact")
    void deathReturnsEnchantmentFromOwnGraveyard() {
        Card enchantment = new Pacifism();
        Card opposingArtifact = new Spellbook();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setGraveyard(player2, List.of(opposingArtifact));

        killCavalier();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertNotInGraveyard(player1, "Pacifism");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("Death trigger requires a target when an eligible card exists")
    void deathCannotDeclineMandatoryTarget() {
        Card artifact = new Spellbook();
        harness.setGraveyard(player1, List.of(artifact));

        killCavalier();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.assertInHand(player1, "Spellbook");
    }

    @Test
    @DisplayName("Death trigger without an eligible target is removed from the stack")
    void deathWithoutEligibleTargetLeavesNoAbilityOnStack() {
        harness.setGraveyard(player1, List.of(new RuneclawBear()));

        killCavalier();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Death trigger cannot choose a replacement when its graveyard target disappears")
    void deathDoesNotRetargetMissingCard() {
        Card artifact = new Spellbook();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(artifact, enchantment));

        killCavalier();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of(enchantment));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Spellbook");
        harness.assertNotInHand(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Pacifism");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castCavalier(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new CavalierOfDawn()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void killCavalier() {
        harness.addToBattlefield(player1, new CavalierOfDawn());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Cavalier of Dawn"));
    }
}
