package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RallyTheMonasteryTest extends BaseCardTest {

    @Test
    @DisplayName("Token mode creates two Monk tokens with prowess")
    void createsMonksWithProwess() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> monks = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(monks).hasSize(2);
        for (Permanent monk : monks) {
            assertThat(monk.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(monk.getCard().getSubtypes()).containsExactly(CardSubtype.MONK);
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(1);
        }

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        for (Permanent monk : monks) {
            assertThat(gqs.getEffectivePower(gd, monk)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, monk)).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("The pump mode gives up to two controlled creatures +2/+2")
    void pumpsUpToTwoControlledCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The destroy mode destroys a creature with power 4 or greater")
    void destroysLargeCreature() {
        Permanent target = addCreatureReady(player2, new CrawWurm());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        harness.castModalInstant(player1, 0, 2, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Craw Wurm");
    }

    @Test
    @DisplayName("The destroy mode rejects a creature below power 4")
    void rejectsSmallCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        addFullMana();

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 2, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("The spell costs two less after casting another spell this turn")
    void reducedCostAfterAnotherSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("The spell still needs its full cost without another spell")
    void needsFullCostWithoutAnotherSpell() {
        harness.setHand(player1, List.of(new RallyTheMonastery()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castModalInstant(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
