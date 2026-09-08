package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HourOfNeedTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each target creature and gives its controller a Sphinx")
    void exilesTargetsAndCreatesSphinxesForTheirControllers() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(ownCreature.getId(), opponentCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Hill Giant");

        assertThat(sphinxesControlledBy(player1.getId())).hasSize(1);
        assertThat(sphinxesControlledBy(player2.getId())).hasSize(1);
        for (Permanent sphinx : List.of(
                sphinxesControlledBy(player1.getId()).getFirst(),
                sphinxesControlledBy(player2.getId()).getFirst())) {
            assertThat(sphinx.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(sphinx.getCard().getSubtypes()).containsExactly(CardSubtype.SPHINX);
            assertThat(gqs.getEffectivePower(gd, sphinx)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, sphinx)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, sphinx, Keyword.FLYING)).isTrue();
        }
    }

    @Test
    @DisplayName("Strive requires {1}{U} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new HillGiant());

        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("May target no creatures")
    void noTargetsExilesNothing() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(sphinxesControlledBy(player1.getId())).isEmpty();
        assertThat(sphinxesControlledBy(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new HourOfNeed()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID fountainId = harness.getPermanentId(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private List<Permanent> sphinxesControlledBy(UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(permanent -> "Sphinx".equals(permanent.getCard().getName()))
                .toList();
    }
}
