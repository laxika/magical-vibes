package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WretchedConfluence.class, GiantSpider.class, GrizzlyBears.class, Spellbook.class})
class WretchedConfluenceTest extends BaseCardTest {

    @Test
    void resolvesAllThreeModes() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        GrizzlyBears returned = new GrizzlyBears();
        GrizzlyBears drawn = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(returned, new Spellbook()));
        harness.setLibrary(player2, List.of(drawn));
        harness.setLife(player2, 20);

        cast(new int[]{0, 1, 2}, List.of(player2.getId(), creature.getId(), returned.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(drawn.getId()));
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(returned.getId()));
        assertThat(creature.getEffectivePower()).isEqualTo(0);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @Test
    void repeatedDrawModeDrawsThreeCardsAndLosesThreeLife() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setLife(player2, 20);

        cast(new int[]{0, 0, 0}, List.of(player2.getId(), player2.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .allSatisfy(handCard -> assertThat(handCard).isNotNull())
                .anyMatch(card -> card.getId().equals(first.getId()))
                .anyMatch(card -> card.getId().equals(second.getId()))
                .anyMatch(card -> card.getId().equals(third.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void creatureModeRejectsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());

        assertThatThrownBy(() -> cast(new int[]{1, 1, 1},
                List.of(artifact.getId(), artifact.getId(), artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int[] modeIndices, List<java.util.UUID> targets) {
        harness.setHand(player1, List.of(new WretchedConfluence()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        gs.playCard(gd, player1, 0,
                ChooseOneEffect.encodeRepeatedModeSelection(3, modeIndices),
                null, null, targets, List.of());
    }
}
