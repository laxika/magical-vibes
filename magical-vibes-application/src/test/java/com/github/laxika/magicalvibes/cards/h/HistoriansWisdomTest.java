package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HistoriansWisdom.class, ColossalDreadmaw.class, FountainOfYouth.class, Forest.class, GrizzlyBears.class})
class HistoriansWisdomTest extends BaseCardTest {

    @Test
    void drawsAndBoostsWhenEnchantedCreatureHasGreatestPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void onlyBoostsWhenAnotherCreatureHasGreaterPower() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new ColossalDreadmaw());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void canEnchantNoncreatureArtifactWithoutDrawingOrBoosting() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setLibrary(player1, List.of(new Forest()));
        castOn(artifact);

        assertThat(gqs.isCreature(gd, artifact)).isFalse();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    void cannotEnchantLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new HistoriansWisdom()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private void castOn(Permanent target) {
        harness.setHand(player1, List.of(new HistoriansWisdom()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
