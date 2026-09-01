package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceTheMindSculptor;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
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

@CardUsed({SyrGingerTheMealEnder.class, JaceTheMindSculptor.class, Naturalize.class, MindStone.class})
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
