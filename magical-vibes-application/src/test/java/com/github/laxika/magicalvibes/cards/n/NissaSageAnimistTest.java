package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaSageAnimist.class, Forest.class, Mountain.class, LeafGilder.class})
class NissaSageAnimistTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a revealed land card onto the battlefield")
    void plusOnePutsLandOntoBattlefield() {
        Permanent nissa = addReadyNissa(player1, 3);
        setLibrary(new Forest(), new LeafGilder());

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
        setLibrary(new LeafGilder(), new Forest());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leaf Gilder");
        harness.assertNotOnBattlefield(player1, "Leaf Gilder");
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
        harness.addToBattlefield(player1, new LeafGilder());
        Permanent bear = findPermanent(player1, "Leaf Gilder");

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

    @Test
    @DisplayName("+1 with an empty library still adds loyalty without attempting a draw")
    void plusOneWithEmptyLibraryDoesNothingElse() {
        Permanent nissa = addReadyNissa(player1, 3);
        setLibrary();
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(nissa);
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    @DisplayName("The ultimate may target six opposing lands and does not animate unchosen lands")
    void ultimateCanTargetSixOpposingLands() {
        addReadyNissa(player1, 8);
        List<Permanent> targets = java.util.stream.IntStream.range(0, 6)
                .mapToObj(i -> addLand(player2, new Forest())).toList();
        targets.forEach(Permanent::tap);
        Permanent unchosen = addLand(player1, new Mountain());
        unchosen.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                targets.stream().map(Permanent::getId).toList());
        harness.passBothPriorities();

        for (Permanent target : targets) {
            assertThat(target.isTapped()).isFalse();
            assertThat(gqs.isCreature(gd, target)).isTrue();
            assertThat(target.getEffectivePower()).isEqualTo(6);
            assertThat(target.getEffectiveToughness()).isEqualTo(6);
            assertThat(target.getGrantedSubtypes()).contains(CardSubtype.ELEMENTAL);
        }
        assertThat(unchosen.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, unchosen)).isFalse();
    }

    @Test
    @DisplayName("The ultimate permits choosing zero lands")
    void ultimateAllowsZeroTargets() {
        Permanent nissa = addReadyNissa(player1, 8);
        Permanent forest = addLand(player1, new Forest());
        forest.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 2, List.of());
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(forest.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("The ultimate rejects more than six lands")
    void ultimateRejectsSevenTargets() {
        Permanent nissa = addReadyNissa(player1, 8);
        List<Permanent> lands = java.util.stream.IntStream.range(0, 7)
                .mapToObj(i -> addLand(player1, new Forest())).toList();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 2,
                lands.stream().map(Permanent::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(8);
    }

    @Test
    @DisplayName("The ultimate still resolves for a remaining target after another land leaves")
    void ultimateResolvesForRemainingTarget() {
        addReadyNissa(player1, 8);
        Permanent forest = addLand(player1, new Forest());
        Permanent mountain = addLand(player1, new Mountain());
        mountain.tap();
        harness.activateAbilityWithMultiTargets(player1, 0, 2,
                List.of(forest.getId(), mountain.getId()));

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, forest));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(mountain.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, mountain)).isTrue();
        assertThat(mountain.getEffectivePower()).isEqualTo(6);
        assertThat(mountain.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Ashaya is a legendary green Elemental creature token")
    void ashayaHasRequiredTokenCharacteristics() {
        addReadyNissa(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Card token = findPermanent(player1, "Ashaya, the Awoken World").getCard();
        assertThat(token.isToken()).isTrue();
        assertThat(token.hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getColors()).containsExactly(CardColor.GREEN);
        assertThat(token.getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(token.getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent perm = addCreatureReady(player, new NissaSageAnimist());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addLand(Player player, Card land) {
        return harness.addToBattlefieldAndReturn(player, land);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
