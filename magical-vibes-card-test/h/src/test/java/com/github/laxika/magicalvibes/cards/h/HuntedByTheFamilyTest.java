package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoldForgedSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.HuntedByTheFamilyEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntedByTheFamily.class, GoldForgedSentinel.class, GrizzlyBears.class})
class HuntedByTheFamilyTest extends BaseCardTest {

    @Test
    void eachTargetControllerChoosesIndependently() {
        Permanent artifactCreature = harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(List.of(artifactCreature.getId(), bears.getId()));

        PendingInteraction.ColorChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(firstChoice.playerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.options()).containsExactly(
                HuntedByTheFamilyEffect.HUMAN_OPTION, HuntedByTheFamilyEffect.COPY_OPTION);

        harness.handleListChoice(player2, HuntedByTheFamilyEffect.HUMAN_OPTION);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player2, HuntedByTheFamilyEffect.COPY_OPTION);

        assertThat(gqs.getEffectiveCardTypes(gd, artifactCreature)).containsExactly(CardType.CREATURE);
        assertThat(gqs.getEffectiveColors(gd, artifactCreature)).containsExactly(CardColor.WHITE);
        assertThat(gqs.effectiveCreatureSubtypes(gd, artifactCreature)).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, artifactCreature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, artifactCreature, Keyword.FLYING))
                .isFalse();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    void cannotTargetACreatureYouControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.castSorcery(player1, 0, List.of(ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canResolveWithoutChoosingAnyTargets() {
        cast(List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Hunted by The Family");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void allFourTargetsCanChooseCopies() {
        List<Permanent> targets = java.util.stream.IntStream.range(0, 4)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel()))
                .toList();

        cast(targets.stream().map(Permanent::getId).toList());
        for (int i = 0; i < 4; i++) {
            harness.handleListChoice(player2, HuntedByTheFamilyEffect.COPY_OPTION);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(4)
                .allSatisfy(token -> {
                    assertThat(token.getCard().isToken()).isTrue();
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
                });
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactlyElementsOf(targets);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    void skipsATargetThatLeftTheBattlefieldBeforeResolution() {
        Permanent gone = harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel());
        Permanent remaining = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();
        harness.castSorcery(player1, 0, List.of(gone.getId(), remaining.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(gone);
        harness.passBothPriorities();

        harness.handleListChoice(player2, HuntedByTheFamilyEffect.COPY_OPTION);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token ->
                assertThat(token.getCard().getName()).isEqualTo("Grizzly Bears"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseMoreThanFourTargets() {
        List<java.util.UUID> targets = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId())
                .toList();
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();

        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> harness.castSorcery(player1, 0, targets))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void humanTransformationKeepsCountersAndCopyUsesOriginalCharacteristics() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player2, new GoldForgedSentinel());
        sentinel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        cast(List.of(sentinel.getId()));
        harness.handleListChoice(player2, HuntedByTheFamilyEffect.HUMAN_OPTION);

        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sentinel)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, sentinel, Keyword.FLYING)).isFalse();

        cast(List.of(sentinel.getId()));
        harness.handleListChoice(player2, HuntedByTheFamilyEffect.COPY_OPTION);

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectiveCardTypes(gd, token)).contains(CardType.ARTIFACT, CardType.CREATURE);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
            assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        });
        assertThat(gqs.getEffectivePower(gd, sentinel)).isEqualTo(3);
    }

    private void cast(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new HuntedByTheFamily()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
