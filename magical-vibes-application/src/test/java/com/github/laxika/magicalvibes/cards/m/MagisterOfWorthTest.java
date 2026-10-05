package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagisterOfWorth.class, GrizzlyBears.class, SolRing.class})
class MagisterOfWorthTest extends BaseCardTest {

    @Test
    void graceMajorityReturnsEachPlayersCreatureCards() {
        Card player1Creature = new GrizzlyBears();
        Card player2Creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(player1Creature));
        harness.setGraveyard(player2, List.of(player2Creature));
        castMagister(new MagisterOfWorth());

        assertThat(activeVote().playerId()).isEqualTo(player1.getId());
        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        assertThat(activeVote().playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.GRACE);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == player2Creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void condemnationWinsOnTieAndDestroysAllOtherCreatures() {
        Permanent player1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent player2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card magister = new MagisterOfWorth();
        castMagister(magister);

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == magister)
                .findFirst().orElseThrow();

        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.CONDEMNATION);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(player1Creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(player2Creature);
    }

    @Test
    void graceReturnsAllCreaturesButLeavesNoncreatureCardsInGraveyards() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        Card firstArtifact = new SolRing();
        Card opposingArtifact = new SolRing();
        harness.setGraveyard(player1, List.of(firstCreature, firstArtifact, secondCreature));
        harness.setGraveyard(player2, List.of(opposingArtifact, opposingCreature));
        castMagister(new MagisterOfWorth());

        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(firstCreature, firstArtifact, secondCreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(opposingArtifact, opposingCreature);
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.GRACE);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(firstCreature, secondCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).containsExactly(opposingCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingArtifact);
    }

    @Test
    void condemnationMajorityDestroysOtherMagistersButPreservesNoncreatures() {
        Permanent otherMagister = harness.addToBattlefieldAndReturn(player2, new MagisterOfWorth());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SolRing());
        Card buriedCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(buriedCreature));
        Card source = new MagisterOfWorth();
        castMagister(source);

        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.CONDEMNATION);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherMagister, artifact);
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.CONDEMNATION);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(buriedCreature, otherMagister.getCard());
    }

    @Test
    void graceWithEmptyGraveyardsLeavesExistingCreaturesAlone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        Card source = new MagisterOfWorth();
        castMagister(source);

        harness.handleListChoice(player1, ChoiceContext.GraceOrCondemnationChoice.GRACE);
        harness.handleListChoice(player2, ChoiceContext.GraceOrCondemnationChoice.GRACE);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(source);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castMagister(Card magister) {
        harness.castFromHand(player1, magister, "{4}{W}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private PendingInteraction.ColorChoice activeVote() {
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyElementsOf(ChoiceContext.GraceOrCondemnationChoice.OPTIONS);
        return choice;
    }
}
