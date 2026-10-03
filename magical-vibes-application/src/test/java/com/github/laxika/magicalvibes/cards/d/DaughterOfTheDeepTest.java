package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GallantCitizen;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaughterOfTheDeep.class, GallantCitizen.class, MerrowCommerce.class})
class DaughterOfTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Merfolk token on its controller's second draw each turn")
    void createsTokenOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new DaughterOfTheDeep());
        setDeck(player1, 3);

        drawCard(player1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Merfolk");
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates only one token for later draws in the same turn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefieldAndReturn(player1, new DaughterOfTheDeep());
        setDeck(player1, 4);

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();
        drawCard(player1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Makes a target Merfolk unblockable for the turn")
    void makesTargetMerfolkUnblockable() {
        Permanent source = addReadyDaughter(player1);
        Permanent target = addReadyDaughter(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Merfolk creature")
    void cannotTargetNonMerfolk() {
        addReadyDaughter(player1);
        Permanent citizen = addReadyCreature(player2, new GallantCitizen());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, citizen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Merfolk creature");
    }

    @Test
    @DisplayName("Can target a noncreature Merfolk permanent")
    void canTargetNoncreatureMerfolk() {
        addReadyDaughter(player1);
        Permanent commerce = harness.addToBattlefieldAndReturn(player1, new MerrowCommerce());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, commerce.getId());
        harness.passBothPriorities();

        assertThat(commerce.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's Merfolk")
    void canTargetOpponentsMerfolk() {
        Permanent source = addReadyDaughter(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DaughterOfTheDeep());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Opponent draws do not trigger the controller's ability")
    void doesNotTriggerOnOpponentDraws() {
        harness.addToBattlefield(player1, new DaughterOfTheDeep());
        setDeck(player2, 3);

        drawCard(player2);
        drawCard(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The controller's second draw triggers during an opponent's turn")
    void triggersDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DaughterOfTheDeep());
        setDeck(player1, 3);

        drawCard(player1);
        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Counts the first draw even if it happened before this creature entered")
    void countsDrawBeforeEntering() {
        setDeck(player1, 3);
        drawCard(player1);
        harness.addToBattlefield(player1, new DaughterOfTheDeep());

        drawCard(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }

    @Test
    @DisplayName("Does not trigger when entering after the second draw")
    void doesNotTriggerAfterSecondDrawAlreadyHappened() {
        setDeck(player1, 4);
        drawCard(player1);
        drawCard(player1);
        harness.addToBattlefield(player1, new DaughterOfTheDeep());

        drawCard(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A summoning-sick source cannot pay the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new DaughterOfTheDeep());
        source.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(source.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDaughter(Player player) {
        return addReadyCreature(player, new DaughterOfTheDeep());
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void setDeck(Player player, int count) {
        harness.setLibrary(player, java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new GallantCitizen()).toList());
    }

    private void drawCard(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
