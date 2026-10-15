package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.c.CloakAndDagger;
import com.github.laxika.magicalvibes.cards.d.Doublecast;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalGrudge.class, GrizzlyBears.class, Millstone.class, Ornithopter.class, Forest.class,
        Bitterblossom.class, CloakAndDagger.class, Doublecast.class})
class FatalGrudgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices one matching opposing permanent and draws a card")
    void sacrificesMatchingPermanentAndDraws() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent matching = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Card drawnCard = new Forest();

        harness.setLibrary(player1, List.of(drawnCard));
        cast(sacrificed);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(matching)
                .contains(nonmatching);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Opponent chooses among permanents sharing a card type with the sacrificed permanent")
    void opponentChoosesMatchingPermanent() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstMatching = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondMatching = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent nonmatching = harness.addToBattlefieldAndReturn(player2, new Millstone());

        cast(sacrificed);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(firstMatching.getId(), secondMatching.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2, List.of(firstMatching.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(firstMatching)
                .contains(secondMatching, nonmatching);
    }

    @Test
    @DisplayName("Cannot sacrifice a land as the additional cost")
    void cannotSacrificeLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FatalGrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonland");
    }

    @Test
    void drawsEvenWhenOpponentHasNoMatchingPermanent() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(sacrificed);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void artifactCostMakesOpponentSacrificeArtifactInsteadOfCreature() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Millstone());
        harness.addToBattlefield(player2, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(sacrificed);

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player2, "Millstone");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
    }

    @Test
    void artifactCreatureCostAllowsEitherCardTypeButOnlyOneSacrifice() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Card drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));

        cast(sacrificed);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(artifact.getId(), creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(creature, land);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void kindredMatchesEvenWhenOtherCardTypesDiffer() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.addToBattlefield(player2, new CloakAndDagger());
        harness.setLibrary(player1, List.of(new Forest()));

        cast(sacrificed);

        harness.assertNotOnBattlefield(player2, "Cloak and Dagger");
        harness.assertInGraveyard(player2, "Cloak and Dagger");
    }

    @Test
    void spellCopyUsesOriginalSacrificeWithoutPayingCostAgain() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        cast(sacrificed);
        harness.passBothPriorities();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(choice.validIds().getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void cast(Permanent sacrificed) {
        harness.setHand(player1, List.of(new FatalGrudge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorceryWithSacrifice(player1, 0, sacrificed.getId());
        harness.passBothPriorities();
    }
}
