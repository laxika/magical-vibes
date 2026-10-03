package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrassKnuckles;
import com.github.laxika.magicalvibes.cards.b.BrokersAscendancy;
import com.github.laxika.magicalvibes.cards.s.Strangle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleanupCrew.class, BrassKnuckles.class, BrokersAscendancy.class, Strangle.class})
class CleanupCrewTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BrassKnuckles());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target artifact.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brass Knuckles");
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BrokersAscendancy());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target enchantment.");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brokers Ascendancy");
    }

    @Test
    void exilesTargetCardFromAGraveyard() {
        Card card = new Strangle();
        harness.setGraveyard(player2, List.of(card));

        castCleanupCrew();
        harness.handleListChoice(player1, "Exile target card from a graveyard.");
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Strangle");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(card);
    }

    @Test
    void gainsFourLife() {
        castCleanupCrew();
        harness.handleListChoice(player1, "You gain 4 life.");
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    void artifactModeRejectsNonArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BrassKnuckles());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BrokersAscendancy());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target artifact.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    @Test
    void enchantmentModeRejectsArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new BrassKnuckles());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new BrokersAscendancy());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target enchantment.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Brokers Ascendancy");
        harness.assertOnBattlefield(player2, "Brass Knuckles");
    }

    @Test
    void canDestroyControllersArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new BrassKnuckles());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target artifact.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Brass Knuckles");
        harness.assertLife(player1, 20);
    }

    @Test
    void canExileCreatureFromControllersGraveyardWithoutGainingLife() {
        Card card = new CleanupCrew();
        harness.setGraveyard(player1, List.of(card));

        castCleanupCrew();
        harness.handleListChoice(player1, "Exile target card from a graveyard.");
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Cleanup Crew");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void cannotChooseGraveyardModeWhenBothGraveyardsAreEmpty() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castCleanupCrew();

        assertThatThrownBy(() -> harness.handleListChoice(player1, "Exile target card from a graveyard."))
                .isInstanceOf(IllegalArgumentException.class);

        harness.handleListChoice(player1, "You gain 4 life.");
        harness.passBothPriorities();
        harness.assertLife(player1, 24);
    }

    @Test
    void missingGraveyardTargetDoesNotExileAnotherCardOrGainLife() {
        Card target = new Strangle();
        Card other = new CleanupCrew();
        harness.setGraveyard(player2, List.of(target, other));

        castCleanupCrew();
        harness.handleListChoice(player1, "Exile target card from a graveyard.");
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Cleanup Crew");
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target, other);
        harness.assertLife(player1, 20);
    }

    private void castCleanupCrew() {
        harness.castFromHand(player1, new CleanupCrew(), "{4}{G}{G}");
        harness.passBothPriorities();
    }
}
