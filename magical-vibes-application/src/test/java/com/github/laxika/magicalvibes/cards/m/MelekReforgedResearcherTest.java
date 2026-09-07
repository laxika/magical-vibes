package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MelekReforgedResearcher.class, Divination.class, GrizzlyBears.class})
class MelekReforgedResearcherTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness are twice the instant and sorcery count in your graveyard")
    void powerAndToughnessScaleWithOwnGraveyard() {
        Permanent melek = addCreatureReady(player1, new MelekReforgedResearcher());
        harness.setGraveyard(player1, List.of(new Divination(), new Divination(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, melek)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, melek)).isEqualTo(4);

        gd.playerGraveyards.get(player1.getId()).add(new Divination());

        assertThat(gqs.getEffectivePower(gd, melek)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, melek)).isEqualTo(6);
    }

    @Test
    @DisplayName("Only your instant and sorcery cards count")
    void ignoresOtherCardTypesAndOpponentsGraveyards() {
        Permanent melek = addCreatureReady(player1, new MelekReforgedResearcher());
        harness.setGraveyard(player1, List.of(new Divination(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Divination(), new Divination()));

        assertThat(gqs.getEffectivePower(gd, melek)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, melek)).isEqualTo(2);
    }

    @Test
    @DisplayName("The first instant or sorcery spell each turn costs three less")
    void reducesOnlyFirstMatchingSpellEachTurn() {
        harness.setGraveyard(player1, List.of(new Divination()));
        harness.setHand(player1, List.of(
                new MelekReforgedResearcher(),
                new GrizzlyBears(),
                new Divination(),
                new Divination()
        ));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
