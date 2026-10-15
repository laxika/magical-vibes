package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.t.ToughCookie;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SyrGingerTheMealEnder.class, JaceTheMindSculptor.class, Naturalize.class, MindStone.class,
        ToughCookie.class})
class SyrGingerTheMealEnderTest extends BaseCardTest {

    @Test
    @DisplayName("Has trample, hexproof, and haste while an opponent controls a planeswalker")
    void gainsKeywordsWhileOpponentControlsPlaneswalker() {
        Permanent ginger = addReadyGinger();
        harness.addToBattlefield(player2, new JaceTheMindSculptor());

        assertThat(gqs.hasKeyword(gd, ginger, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Does not have the conditional keywords without an opposing planeswalker")
    void lacksKeywordsWithoutOpponentPlaneswalker() {
        Permanent ginger = addReadyGinger();

        assertThat(gqs.hasKeyword(gd, ginger, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An artifact you control puts a counter on Syr Ginger and starts scrying")
    void ownArtifactGraveyardTriggerPutsCounterAndScries() {
        Permanent ginger = addReadyGinger();
        harness.addToBattlefield(player1, new MindStone());
        destroyArtifact(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(ginger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's artifact does not trigger Syr Ginger")
    void opponentArtifactDoesNotTrigger() {
        Permanent ginger = addReadyGinger();
        harness.addToBattlefield(player2, new MindStone());
        destroyArtifact(player2);
        harness.passBothPriorities();

        assertThat(ginger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing Syr Ginger gains life equal to its power")
    void sacrificeAbilityGainsLifeEqualToPower() {
        Permanent ginger = addReadyGinger();
        ginger.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Syr Ginger, the Meal Ender");
        harness.assertLife(player1, 5 + 20);
    }

    @Test
    @DisplayName("A planeswalker controlled by Syr Ginger's controller does not grant keywords")
    void ownPlaneswalkerDoesNotGrantKeywords() {
        Permanent ginger = addReadyGinger();
        harness.addToBattlefield(player1, new JaceTheMindSculptor());

        assertThat(gqs.hasKeyword(gd, ginger, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Conditional keywords disappear when the opposing planeswalker leaves")
    void losesKeywordsWhenOpposingPlaneswalkerLeaves() {
        Permanent ginger = addReadyGinger();
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheMindSculptor());
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HASTE)).isTrue();

        jace.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player2, "Jace, the Mind Sculptor");
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, ginger, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Syr Ginger's own death does not trigger its artifact ability")
    void ownDeathDoesNotTrigger() {
        Permanent ginger = addReadyGinger();
        ginger.setMarkedDamage(1);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Syr Ginger, the Meal Ender");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Simultaneous deaths trigger only for the other artifact and still allow scrying")
    void simultaneousArtifactDeathsTriggerOnlyForOtherArtifact() {
        Permanent ginger = addReadyGinger();
        Permanent cookie = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        harness.setLibrary(player1, List.of(new SyrGingerTheMealEnder()));
        ginger.setMarkedDamage(1);
        cookie.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Syr Ginger, the Meal Ender");
        harness.assertInGraveyard(player1, "Tough Cookie");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A pending artifact trigger still scries after Syr Ginger is sacrificed")
    void pendingArtifactTriggerScriesAfterSourceLeaves() {
        addReadyGinger();
        Permanent cookie = harness.addToBattlefieldAndReturn(player1, new ToughCookie());
        harness.setLibrary(player1, List.of(new SyrGingerTheMealEnder()));
        cookie.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Conditional haste permits the sacrifice ability on a summoning-sick Syr Ginger")
    void opposingPlaneswalkerAllowsImmediateSacrifice() {
        Permanent ginger = harness.addToBattlefieldAndReturn(player1, new SyrGingerTheMealEnder());
        ginger.setSummoningSick(true);
        harness.addToBattlefield(player2, new JaceTheMindSculptor());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Syr Ginger, the Meal Ender");
        harness.assertInGraveyard(player1, "Syr Ginger, the Meal Ender");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Without conditional haste a summoning-sick Syr Ginger cannot pay its tap cost")
    void summoningSicknessPreventsSacrificeWithoutOpposingPlaneswalker() {
        Permanent ginger = harness.addToBattlefieldAndReturn(player1, new SyrGingerTheMealEnder());
        ginger.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Syr Ginger, the Meal Ender");
        assertThat(ginger.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyGinger() {
        return addCreatureReady(player1, new SyrGingerTheMealEnder());
    }

    private void destroyArtifact(com.github.laxika.magicalvibes.model.Player artifactController) {
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0,
                harness.getPermanentId(artifactController, "Mind Stone"));
    }
}
