package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DayOfBlackSun;
import com.github.laxika.magicalvibes.cards.z.ZukosExile;
import com.github.laxika.magicalvibes.cards.o.OneWithTheStars;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({BaSingSe.class, Forest.class, DayOfBlackSun.class, ZukosExile.class, OneWithTheStars.class})
class BaSingSeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped when you control no basic land")
    void entersTappedWithoutBasicLand() {
        playBaSingSe();

        assertThat(findPermanent(player1, "Ba Sing Se").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped when you control a basic land")
    void entersUntappedWithBasicLand() {
        harness.addToBattlefield(player1, new Forest());
        playBaSingSe();

        assertThat(findPermanent(player1, "Ba Sing Se").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapping produces one green mana")
    void tappingProducesGreenMana() {
        addBaSingSeReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sorcery-speed ability earthbends a land twice")
    void earthbendsTargetLand() {
        Permanent source = addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        prepareEarthbend();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, target)).isTrue();
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Permanent addBaSingSeReady(Player player) {
        return addCreatureReady(player, new BaSingSe());
    }

    @Test
    void opponentsBasicLandDoesNotAllowUntappedEntry() {
        harness.addToBattlefield(player2, new Forest());
        playBaSingSe();

        assertThat(findPermanent(player1, "Ba Sing Se").isTapped()).isTrue();
    }

    @Test
    void ownNonbasicLandDoesNotAllowUntappedEntry() {
        addBaSingSeReady(player1);
        playBaSingSe();

        assertThat(findPermanents(player1, "Ba Sing Se").getLast().isTapped()).isTrue();
    }

    @Test
    void basicPermanentThatIsNoLongerALandDoesNotAllowUntappedEntry() {
        addBaSingSeReady(player1);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(forest);
        harness.setHand(player1, List.of(new OneWithTheStars()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        assertThat(gqs.isLand(gd, forest)).isFalse();

        playBaSingSe();

        assertThat(findPermanents(player1, "Ba Sing Se").getLast().isTapped()).isTrue();
    }

    @Test
    void cannotEarthbendOpponentsLand() {
        addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareEarthbend();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEarthbendOutsideMainPhase() {
        Permanent source = addBaSingSeReady(player1);
        prepareEarthbend();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotEarthbendWithAnAbilityOnTheStack() {
        Permanent source = addBaSingSeReady(player1);
        prepareEarthbend();
        harness.activateAbility(player1, 0, 1, null, source.getId());
        source.untap();
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canEarthbendItselfAndRetainsManaAbility() {
        Permanent source = addBaSingSeReady(player1);
        earthbend(source);

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, source)).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        source.untap();
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(greenBefore + 1);
    }

    @Test
    void repeatedEarthbendAddsCounters() {
        Permanent source = addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(target);
        source.untap();
        earthbend(target);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    void earthbendedLandReturnsTappedAfterDyingAsANewNoncreatureLand() {
        addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(target);
        target.setMarkedDamage(2);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertReturnedForest(target);
    }

    @Test
    void earthbendedLandReturnsTappedAfterExile() {
        addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(target);
        harness.setHand(player1, List.of(new ZukosExile()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertReturnedForest(target);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void losingAbilitiesDoesNotRemoveEarthbendsDelayedReturn() {
        addBaSingSeReady(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Forest());
        earthbend(target);
        harness.setHand(player1, List.of(new DayOfBlackSun()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertReturnedForest(target);
    }

    private void assertReturnedForest(Permanent oldLand) {
        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(oldLand.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isLand(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    private void earthbend(Permanent target) {
        prepareEarthbend();
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
    }

    private void prepareEarthbend() {
        prepareMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void playBaSingSe() {
        harness.setHand(player1, List.of(new BaSingSe()));
        prepareMainPhase();
        harness.playLand(player1, 0);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
