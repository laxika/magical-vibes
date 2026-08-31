package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Bonesplitter;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleanupCrew.class, Bonesplitter.class, GloriousAnthem.class, Shock.class})
class CleanupCrewTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target artifact.");
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bonesplitter");
    }

    @Test
    void destroysTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target enchantment.");
        harness.handlePermanentChosen(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void exilesTargetCardFromAGraveyard() {
        Card card = new Shock();
        harness.setGraveyard(player2, List.of(card));

        castCleanupCrew();
        harness.handleListChoice(player1, "Exile target card from a graveyard.");
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Shock");
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Bonesplitter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        castCleanupCrew();
        harness.handleListChoice(player1, "Destroy target artifact.");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
    }

    private void castCleanupCrew() {
        harness.setHand(player1, List.of(new CleanupCrew()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
