package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArrowStorm;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.w.WeaveFate;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlOfTheHorde.class, Divination.class, ArrowStorm.class, WeaveFate.class})
class HowlOfTheHordeTest extends BaseCardTest {

    @Test
    @DisplayName("Copies the next instant or sorcery spell once without raid")
    void copiesNextSpellOnceWithoutRaid() {
        castHowlAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);

        castDivinationAndResolve();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Copies the next instant or sorcery spell twice with raid")
    void copiesNextSpellTwiceWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castHowlAndResolve();

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(2);

        castDivinationAndResolve();

        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Only the first qualifying spell is copied")
    void onlyCopiesFirstQualifyingSpell() {
        castHowlAndResolve();
        castDivinationAndResolve();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        castDivinationAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A copied Howl creates another delayed copy effect without being cast")
    void copiesOfHowlCreateAdditionalDelayedEffects() {
        harness.setHand(player1, List.of(new HowlOfTheHorde(), new HowlOfTheHorde(), new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
        castDivinationAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("The copy may keep the original target")
    void copyKeepsOriginalTarget() {
        prepareArrowStorm();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 12);
    }

    @Test
    @DisplayName("The copy may choose a different target")
    void copyMayChooseDifferentTarget() {
        prepareArrowStorm();
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Attacking after Howl resolves does not create the raid copy")
    void raidIsCheckedWhenHowlResolves() {
        castHowlAndResolve();
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());

        castDivinationAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("An opponent's instant does not consume your delayed copy effect")
    void onlyCopiesControllersInstant() {
        harness.setHand(player1, List.of(new HowlOfTheHorde(), new WeaveFate()));
        harness.setHand(player2, List.of(new WeaveFate()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.castInstant(player2, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);

        harness.castInstant(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    private void prepareArrowStorm() {
        harness.setHand(player1, List.of(new HowlOfTheHorde(), new ArrowStorm()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void castHowlAndResolve() {
        harness.setHand(player1, List.of(new HowlOfTheHorde(), new Divination()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void castDivinationAndResolve() {
        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();
    }
}
