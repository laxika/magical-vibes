package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParapetThrasher.class, Forest.class, SolRing.class})
class ParapetThrasherTest extends BaseCardTest {

    private static final String DESTROY = "Destroy target artifact that opponent controls";
    private static final String DAMAGE = "This creature deals 4 damage to each other opponent";
    private static final String EXILE = "Exile the top card of your library. You may play it this turn";

    @Test
    @DisplayName("The destroy mode can target only an artifact controlled by the damaged opponent")
    void destroyModeTargetsDamagedOpponentArtifact() {
        addCreatureReady(player1, new ParapetThrasher());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new SolRing());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new SolRing());

        declareAttackers(List.of(0));
        chooseMode(DESTROY);

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(opponentArtifact.getId())
                .doesNotContain(ownArtifact.getId());

        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("The damage mode is available after a Dragon deals combat damage")
    void damageModeResolves() {
        addCreatureReady(player1, new ParapetThrasher());

        declareAttackers(List.of(0));
        chooseMode(DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("The exile mode exiles the top card with permission to play it this turn")
    void exileModeGrantsPlayPermission() {
        addCreatureReady(player1, new ParapetThrasher());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    private void chooseMode(String mode) {
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
    }
}
