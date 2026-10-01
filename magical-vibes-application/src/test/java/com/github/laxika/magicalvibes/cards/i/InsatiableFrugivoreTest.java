package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InsatiableFrugivore.class, GrizzlyBears.class})
class InsatiableFrugivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Food and can decline the graveyard repeat")
    void createsFoodAndCanDeclineRepeat() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castFrugivore();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Exiling three cards creates another Food and repeats")
    void exilingThreeCardsRepeatsProcess() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castFrugivore();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificed Foods give creatures +X/+0 and menace")
    void sacrificedFoodsPumpCreatures() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent frugivore = castFrugivore();
        int frugivoreIndex = gd.playerBattlefields.get(player1.getId()).indexOf(frugivore);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, frugivoreIndex, 0, null, null);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class)).isNotNull();
        harness.handleXValueChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isZero();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, frugivore, Keyword.MENACE)).isTrue();
    }

    private Permanent castFrugivore() {
        harness.setHand(player1, List.of(new InsatiableFrugivore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Insatiable Frugivore");
    }
}
