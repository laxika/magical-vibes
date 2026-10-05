package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.ContestedCliffs;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.r.RavenousBaloth;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanGroundshaker.class, RavenousBaloth.class, ElvishWarrior.class, ContestedCliffs.class})
class KrosanGroundshakerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants trample to a target Beast creature")
    void grantsTrampleToBeast() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Can target a Beast creature controlled by an opponent")
    void grantsTrampleToOpponentsBeast() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player2, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The granted trample wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-Beast creature")
    void cannotTargetNonBeastCreature() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent nonBeast = addCreatureReady(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, nonBeast.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Beast creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new ContestedCliffs());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Beast creature");
    }

    @Test
    @DisplayName("Can grant itself trample while summoning sick")
    void canTargetItselfWhileSummoningSick() {
        Permanent groundshaker = harness.addToBattlefieldAndReturn(player1, new KrosanGroundshaker());
        groundshaker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, groundshaker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, groundshaker, Keyword.TRAMPLE)).isTrue();
        assertThat(groundshaker.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can activate while tapped")
    void canActivateWhileTapped() {
        Permanent groundshaker = addCreatureReady(player1, new KrosanGroundshaker());
        groundshaker.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, groundshaker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, groundshaker, Keyword.TRAMPLE)).isTrue();
        assertThat(groundshaker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("One green mana pays for exactly one activation")
    void eachActivationRequiresGreenMana() {
        Permanent groundshaker = addCreatureReady(player1, new KrosanGroundshaker());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, groundshaker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, groundshaker, Keyword.TRAMPLE)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, groundshaker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not grant trample when the target is sacrificed in response")
    void targetSacrificedInResponse() {
        addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handlePermanentChosen(player1, beast.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ravenous Baloth");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability still resolves after its source is sacrificed")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent groundshaker = addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handlePermanentChosen(player1, groundshaker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Krosan Groundshaker");
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can activate multiple times without tapping")
    void canGrantTrampleToTwoBeasts() {
        Permanent groundshaker = addCreatureReady(player1, new KrosanGroundshaker());
        Permanent beast = addCreatureReady(player1, new RavenousBaloth());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, groundshaker.getId());
        harness.activateAbility(player1, 0, 0, null, beast.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, groundshaker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, beast, Keyword.TRAMPLE)).isTrue();
        assertThat(groundshaker.isTapped()).isFalse();
    }
}
