package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.t.ThunderbreakRegent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParapetThrasher.class, Forest.class, SolRing.class, ThunderbreakRegent.class})
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


    @Test
    @DisplayName("Another Dragon triggers the ability while Parapet Thrasher stays out of combat")
    void anotherDragonTriggersAbility() {
        harness.addToBattlefield(player1, new ParapetThrasher());
        addCreatureReady(player1, new ThunderbreakRegent());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(List.of(1));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two Dragons dealing simultaneous damage produce only one trigger per Thrasher")
    void simultaneousDragonsTriggerOnlyOnce() {
        addCreatureReady(player1, new ParapetThrasher());
        addCreatureReady(player1, new ThunderbreakRegent());
        Forest topCard = new Forest();
        SolRing secondCard = new SolRing();
        harness.setLibrary(player1, List.of(topCard, secondCard));

        declareAttackers(List.of(0, 1));
        chooseMode(EXILE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A chosen mode is unavailable in a later combat during the same turn")
    void modeCannotBeRepeatedThisTurn() {
        Permanent thrasher = addCreatureReady(player1, new ParapetThrasher());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        thrasher.setTapped(false);
        declareAttackers(List.of(0));
        resolveCombat();
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).contains(DESTROY, DAMAGE).doesNotContain(EXILE);
        harness.handleListChoice(player1, DESTROY);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("The same mode can be chosen again on a later turn")
    void modeBecomesAvailableNextTurn() {
        addCreatureReady(player1, new ParapetThrasher());
        declareAttackers(List.of(0));
        chooseMode(DAMAGE);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(List.of(0));
        chooseMode(DAMAGE);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exiled land can be played in the postcombat main phase")
    void exiledLandCanBePlayed() {
        addCreatureReady(player1, new ParapetThrasher());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.castFromExile(player1, topCard.getId());

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(topCard.getId()));
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Exile permission expires at the end of the turn even if the card remains exiled")
    void exilePermissionExpires() {
        addCreatureReady(player1, new ParapetThrasher());
        Forest topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        declareAttackers(List.of(0));
        chooseMode(EXILE);
        resolveAllTriggers();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
    }

    private void chooseMode(String mode) {
        resolveCombat();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, mode);
    }
}
