package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ChiefEngineer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaintTraftAndRemKarolus.class, StokeTheFlames.class, ChiefEngineer.class, SolRing.class})
class SaintTraftAndRemKarolusTest extends BaseCardTest {

    @Test
    @DisplayName("Its first three tap-trigger resolutions create the Human, Spirit, and Angel tokens")
    void tapTriggerCreatesProgressiveTokens() {
        Permanent saintTraftAndRemKarolus = harness.addToBattlefieldAndReturn(
                player1, new SaintTraftAndRemKarolus());

        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent human = findPermanent(player1, "Human");
        assertThat(human.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(human.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
        assertThat(human.getEffectivePower()).isEqualTo(1);
        assertThat(human.getEffectiveToughness()).isEqualTo(1);

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(spirit.hasKeyword(Keyword.FLYING)).isTrue();

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        Permanent angel = findPermanent(player1, "Angel");
        assertThat(angel.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(angel.getCard().getSubtypes()).containsExactly(CardSubtype.ANGEL);
        assertThat(angel.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(angel.getEffectivePower()).isEqualTo(4);
        assertThat(angel.getEffectiveToughness()).isEqualTo(4);

        saintTraftAndRemKarolus.untap();
        tap(saintTraftAndRemKarolus);
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("Casting a convoke spell untaps it")
    void convokeSpellUntapsSource() {
        Permanent saintTraftAndRemKarolus = harness.addToBattlefieldAndReturn(
                player1, new SaintTraftAndRemKarolus());
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstantWithConvoke(player1, 0, List.of(player2.getId()),
                List.of(saintTraftAndRemKarolus.getId()));
        assertThat(saintTraftAndRemKarolus.isTapped()).isTrue();

        resolveAllTriggers();

        assertThat(saintTraftAndRemKarolus.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Queued tap triggers each use their own resolution number")
    void queuedTapTriggersCreateDifferentTokens() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());

        for (int i = 0; i < 3; i++) {
            source.untap();
            tap(source);
        }
        assertThat(findPermanents(player1, "Human")).isEmpty();

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Angel")).hasSize(1);
    }

    @Test
    @DisplayName("The first tap-trigger resolution on the next turn creates another Human")
    void resolutionCountResetsOnNextTurn() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());
        tap(source);
        resolveAllTriggers();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        source.untap();
        tap(source);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human")).hasSize(2);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger the token ability")
    void tappingAnotherCreatureDoesNotCreateToken() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());
        tap(source);
        resolveAllTriggers();

        tap(findPermanent(player1, "Human"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("A convoke spell paid entirely with mana still untaps the source")
    void convokeSpellWithoutConvokingUntapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());
        tap(source);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new StokeTheFlames()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(source.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(source.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Human")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("An opponent's convoke spell does not untap the source")
    void opponentsConvokeSpellDoesNotUntapSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());
        tap(source);
        resolveAllTriggers();
        harness.setHand(player2, List.of(new StokeTheFlames()));
        harness.addMana(player2, ManaColor.RED, 4);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an artifact with convoke granted by Chief Engineer untaps the source")
    void grantedConvokeUntapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new SaintTraftAndRemKarolus());
        harness.addToBattlefield(player1, new ChiefEngineer());
        tap(source);
        resolveAllTriggers();
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(source.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
