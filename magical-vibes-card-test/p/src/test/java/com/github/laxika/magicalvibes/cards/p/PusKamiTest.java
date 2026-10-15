package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BileUrchin;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.t.TeardropKami;
import com.github.laxika.magicalvibes.cards.t.ThatWhichWasTaken;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        PusKami.class,
        BileUrchin.class,
        GoblinCohort.class,
        PatronOfTheKitsune.class,
        TeardropKami.class,
        ThatWhichWasTaken.class
})
class PusKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Ability sacrifices Pus Kami and destroys a nonblack creature")
    void destroysNonblackCreature() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pus Kami");
        harness.assertInGraveyard(player1, "Pus Kami");
        assertThat(countPermanents(player2, "Goblin Cohort")).isZero();
    }

    @Test
    @DisplayName("Ability cannot target a black creature")
    void cannotTargetBlackCreature() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new BileUrchin());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Bile Urchin")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new ThatWhichWasTaken());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "That Which Was Taken")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be a nonblack creature");
    }

    @Test
    @DisplayName("Soulshift 6 returns a targeted Spirit with mana value 6 or less when Pus Kami dies")
    void soulshiftReturnsCheapSpirit() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card spirit = new TeardropKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Teardrop Kami");
        harness.assertNotInGraveyard(player1, "Teardrop Kami");
    }

    @Test
    @DisplayName("Soulshift only offers Spirits with mana value 6 or less from your graveyard")
    void soulshiftFiltersTargets() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card cheapSpirit = new TeardropKami();
        Card boundarySpirit = new PatronOfTheKitsune();
        Card expensiveSpirit = new PusKami();
        Card nonSpirit = new GoblinCohort();
        Card opponentSpirit = new TeardropKami();
        harness.setGraveyard(player1, List.of(cheapSpirit, boundarySpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(cheapSpirit.getId(), boundarySpirit.getId());
        assertThat(choice.validCardIds())
                .doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Soulshift can be declined even when a legal Spirit target exists")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card spirit = new TeardropKami();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Teardrop Kami");
        harness.assertNotInHand(player1, "Teardrop Kami");
    }

    @Test
    @DisplayName("Ability can destroy your own nonblack creature")
    void destroysOwnNonblackCreature() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player1, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Goblin Cohort"));

        harness.assertNotOnBattlefield(player1, "Pus Kami");
        harness.assertOnBattlefield(player1, "Goblin Cohort");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Goblin Cohort");
        harness.assertInGraveyard(player1, "Goblin Cohort");
    }

    @Test
    @DisplayName("Soulshift returns a Spirit with mana value exactly six before the activated ability resolves")
    void soulshiftReturnsBoundarySpiritBeforeDestruction() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card spirit = new PatronOfTheKitsune();
        harness.setGraveyard(player1, List.of(spirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Patron of the Kitsune");
        harness.assertNotInGraveyard(player1, "Patron of the Kitsune");
        harness.assertOnBattlefield(player2, "Goblin Cohort");

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("Soulshift cannot return a target that has left the graveyard or choose a replacement")
    void soulshiftDoesNotRetargetMissingSpirit() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);
        Card target = new TeardropKami();
        Card otherSpirit = new BileUrchin();
        harness.setGraveyard(player1, List.of(target, otherSpirit));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> !card.getId().equals(target.getId()))
                .toList());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Teardrop Kami");
        harness.assertNotInHand(player1, "Bile Urchin");
        harness.assertInGraveyard(player1, "Bile Urchin");
    }

    @Test
    @DisplayName("Soulshift offers no choice with no Spirit in your graveyard")
    void soulshiftNoLegalSpiritNoChoice() {
        harness.addToBattlefield(player1, new PusKami());
        harness.addToBattlefield(player2, new GoblinCohort());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setGraveyard(player1, List.of(new GoblinCohort()));

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Goblin Cohort"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
