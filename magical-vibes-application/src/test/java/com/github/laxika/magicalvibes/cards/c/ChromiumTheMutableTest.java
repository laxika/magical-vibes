package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneAdaptation;
import com.github.laxika.magicalvibes.cards.e.EssenceScatter;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChromiumTheMutable.class, GreenwoodSentinel.class, EssenceScatter.class, Murder.class, TitanicGrowth.class})
class ChromiumTheMutableTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card transforms Chromium until end of turn")
    void discardTransformsChromium() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));

        assertThat(gqs.isUncounterable(gd, chromium.getCard())).isTrue();
        activateChromium(chromium);

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gqs.effectiveCreatureSubtypes(gd, chromium)).containsExactly(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, chromium)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isTrue();
        assertThat(chromium.isCantBeBlocked()).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, chromium)).isEmpty();
    }

    @Test
    @DisplayName("Chromium's transformation wears off at end of turn")
    void transformationWearsOffAtEndOfTurn() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));

        activateChromium(chromium);
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, chromium)).doesNotContain(CardSubtype.HUMAN);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, chromium)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isFalse();
        assertThat(chromium.isCantBeBlocked()).isFalse();
        assertThat(gs.getEffectiveActivatedAbilities(gd, chromium)).hasSize(1);
    }

    @Test
    @DisplayName("Chromium can be cast on the opponent's turn and cannot be countered")
    void flashSpellCannotBeCountered() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        ChromiumTheMutable chromium = new ChromiumTheMutable();
        harness.castFromHand(player1, chromium, "{4}{W}{U}{B}");
        harness.setHand(player2, List.of(new EssenceScatter()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, chromium.getId());

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chromium, the Mutable");
        harness.assertNotInGraveyard(player1, "Chromium, the Mutable");
        harness.assertInGraveyard(player2, "Essence Scatter");
    }

    @Test
    @DisplayName("Discard is paid before the transformation resolves")
    void discardIsAnActivationCost() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Greenwood Sentinel");
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isFalse();

        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(1);
    }

    @Test
    @DisplayName("The discard ability cannot be activated with an empty hand")
    void cannotActivateWithoutDiscard() {
        addChromium(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Transforming in response makes an opponent's removal target illegal")
    void hexproofStopsRemovalAlreadyOnTheStack() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, chromium.getId());
        harness.ensurePriority(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chromium, the Mutable");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Transformation preserves a previously resolved power and toughness boost")
    void transformationPreservesBoost() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new TitanicGrowth(), new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, chromium.getId());

        activateChromium(chromium);

        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, chromium)).isEqualTo(5);
    }

    @Test
    @CardUsed({ArcaneAdaptation.class})
    @DisplayName("A later creature type grant applies after Chromium becomes Human")
    void laterTypeGrantAddsToHumanType() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        activateChromium(chromium);

        harness.castFromHand(player1, new ArcaneAdaptation(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gqs.effectiveCreatureSubtypes(gd, chromium))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.GOBLIN);
        assertThat(gqs.hasEffectiveSubtype(gd, chromium, CardSubtype.GOBLIN)).isTrue();
    }

    @Test
    @DisplayName("Transformed Chromium cannot be blocked by a creature")
    void transformedChromiumCannotBeBlocked() {
        Permanent chromium = addChromium(player1);
        chromium.setSummoningSick(false);
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        harness.setHand(player1, List.of(new GreenwoodSentinel()));
        activateChromium(chromium);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Multiple activations already on the stack still resolve after abilities are lost")
    void stackedActivationsStillResolve() {
        Permanent chromium = addChromium(player1);
        harness.setHand(player1, List.of(new GreenwoodSentinel(), new GreenwoodSentinel()));
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, chromium)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, chromium, Keyword.HEXPROOF)).isTrue();
        assertThat(gs.getEffectiveActivatedAbilities(gd, chromium)).isEmpty();
    }

    private void activateChromium(Permanent chromium) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(chromium), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addChromium(Player player) {
        return harness.addToBattlefieldAndReturn(player, new ChromiumTheMutable());
    }
}
