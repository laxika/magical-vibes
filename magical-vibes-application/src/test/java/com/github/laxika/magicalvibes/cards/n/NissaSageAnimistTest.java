package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NissaSageAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a revealed land card onto the battlefield")
    void plusOnePutsLandOntoBattlefield() {
        Permanent nissa = addReadyNissa(player1, 3);
        setLibrary(new Forest(), new GrizzlyBears());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("+1 puts a revealed nonland card into your hand")
    void plusOnePutsNonlandIntoHand() {
        addReadyNissa(player1, 3);
        setLibrary(new GrizzlyBears(), new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("−2 creates a legendary 4/4 green Elemental named Ashaya, the Awoken World")
    void minusTwoCreatesAshaya() {
        Permanent nissa = addReadyNissa(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        Permanent ashaya = findPermanent(player1, "Ashaya, the Awoken World");
        assertThat(ashaya).isNotNull();
        assertThat(ashaya.getEffectivePower()).isEqualTo(4);
        assertThat(ashaya.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("−7 untaps the targeted lands and makes them 6/6 Elementals that are still lands")
    void ultimateUntapsAndAnimatesLands() {
        Permanent nissa = addReadyNissa(player1, 7);
        Permanent forest = addLand(player1, new Forest());
        Permanent mountain = addLand(player1, new Mountain());
        forest.tap();
        mountain.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(forest.getId(), mountain.getId()));
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isZero();
        for (Permanent land : List.of(forest, mountain)) {
            assertThat(land.isTapped()).isFalse();
            assertThat(gqs.isCreature(gd, land)).isTrue();
            assertThat(land.getEffectivePower()).isEqualTo(6);
            assertThat(land.getEffectiveToughness()).isEqualTo(6);
            assertThat(land.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
            assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        }
    }

    @Test
    @DisplayName("−7 animation has no duration and survives the end of the turn")
    void ultimateAnimationSurvivesEndOfTurn() {
        addReadyNissa(player1, 7);
        Permanent forest = addLand(player1, new Forest());

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of(forest.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(forest.getEffectivePower()).isEqualTo(6);
    }

    @Test
    @DisplayName("−7 cannot target a nonland permanent")
    void ultimateRejectsNonlandTarget() {
        addReadyNissa(player1, 7);
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = findPermanent(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("−7 cannot be activated with insufficient loyalty")
    void ultimateNeedsSevenLoyalty() {
        addReadyNissa(player1, 6);
        Permanent forest = addLand(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = new Permanent(new NissaSageAnimist());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addLand(Player player, Card land) {
        Permanent perm = new Permanent(land);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(new ArrayList<>(List.of(cards)));
    }
}
