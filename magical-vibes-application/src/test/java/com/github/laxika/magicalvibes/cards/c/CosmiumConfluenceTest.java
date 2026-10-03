package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HiddenNursery;
import com.github.laxika.magicalvibes.cards.d.DeeprootPilgrimage;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CosmiumConfluence.class, HiddenNursery.class, DeeprootPilgrimage.class})
class CosmiumConfluenceTest extends BaseCardTest {

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    void repeatedCaveModeResolvesAllThreeSelections() {
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new HiddenNursery());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 1);
        harness.castAndResolveSorcery(player1, 0, modes);

        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(9);
        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, cave)).isEqualTo(9);
        assertThat(gqs.hasKeyword(gd, cave, Keyword.HASTE)).isTrue();
    }

    @Test
    void repeatedDestroyModeUsesOneTargetPerSelection() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2);
        harness.castSorcery(player1, 0, modes, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void repeatedSearchesPutThreeCavesOntoBattlefieldTapped() {
        harness.setLibrary(player1, List.of(new HiddenNursery(), new HiddenNursery(), new HiddenNursery()));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 0));
        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3)
                .allSatisfy(cave -> {
                    assertThat(cave.isTapped()).isTrue();
                    assertThat(gqs.isCreature(gd, cave)).isFalse();
                });
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchedCaveCanBeAnimatedTwiceDuringResolution() {
        harness.setLibrary(player1, List.of(new HiddenNursery()));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 1, 1));
        harness.handleCardChosen(player1, 0);

        Permanent cave = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, cave)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, cave, Keyword.HASTE)).isTrue();
        assertThat(cave.isTapped()).isTrue();
    }

    @Test
    void modesResolveInPrintedOrderRegardlessOfSelectionOrder() {
        harness.setLibrary(player1, List.of(new HiddenNursery()));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 0));
        harness.handleCardChosen(player1, 0);

        Permanent cave = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(6);
    }

    @Test
    void repeatedAnimationsAllowChoosingDifferentCavesEachTime() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HiddenNursery());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HiddenNursery());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new HiddenNursery());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 1, 1));
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, opposing)).isFalse();
    }

    @Test
    void sameEnchantmentCanBeTargetedForAllThreeDestroySelections() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2),
                List.of(enchantment.getId(), enchantment.getId(), enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Deeproot Pilgrimage");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void allIllegalTargetsPreventUntargetedModesFromResolving() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new HiddenNursery());
        harness.setLibrary(player1, List.of(new HiddenNursery()));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 1, 2),
                List.of(enchantment.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(enchantment);
        harness.passBothPriorities();

        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, cave)).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(cave);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Cosmium Confluence");
    }

    @Test
    void failingToFindDoesNotSkipLaterSearchesOrAnimation() {
        harness.setLibrary(player1, List.of(new HiddenNursery()));
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 0, 1));
        harness.handleCardChosen(player1, -1);
        harness.handleCardChosen(player1, 0);

        Permanent cave = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, cave)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, cave)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, cave, CardSubtype.CAVE)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, cave, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void oneRemainingLegalTargetAllowsAnimationAndDestruction() {
        Permanent illegal = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        Permanent legal = harness.addToBattlefieldAndReturn(player2, new DeeprootPilgrimage());
        Permanent cave = harness.addToBattlefieldAndReturn(player1, new HiddenNursery());
        harness.setHand(player1, List.of(new CosmiumConfluence()));
        addMana();

        harness.castSorcery(player1, 0, ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2),
                List.of(illegal.getId(), legal.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(illegal);
        harness.passBothPriorities();

        assertThat(cave.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, cave)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Deeproot Pilgrimage");
    }
}
