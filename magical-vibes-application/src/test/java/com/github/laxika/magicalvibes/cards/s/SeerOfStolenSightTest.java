package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeerOfStolenSight.class, Forest.class, GrizzlyBears.class, Spellbook.class})
class SeerOfStolenSightTest extends BaseCardTest {

    @Test
    void surveilsOnceWhenControlledArtifactAndCreatureDieTogether() {
        harness.addToBattlefield(player1, new SeerOfStolenSight());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().performSimultaneousRemovals(
                gd, List.of(creature, artifact), () -> {
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
                    harness.getPermanentRemovalService().removePermanentToGraveyard(gd, artifact);
                }));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void surveilsWhenAControlledArtifactDiesAlone() {
        harness.addToBattlefield(player1, new SeerOfStolenSight());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void doesNotTriggerForPermanentControlledByOpponent() {
        harness.addToBattlefield(player1, new SeerOfStolenSight());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, artifact));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
