package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkaabWrangler.class, GrizzlyBears.class, Forest.class})
class SkaabWranglerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping three creatures taps the target creature")
    void tappingThreeCreaturesTapsTargetCreature() {
        Permanent wrangler = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureA = addCreatureReady(player1, new GrizzlyBears());
        Permanent creatureB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(wrangler.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without three untapped creatures")
    void cannotActivateWithoutThreeUntappedCreatures() {
        addCreatureReady(player1, new SkaabWrangler());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new SkaabWrangler());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        land.setSummoningSick(false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void summoningSickCreaturesCanPayCostAndActivate() {
        Permanent wrangler = harness.addToBattlefieldAndReturn(player1, new SkaabWrangler());
        Permanent creatureA = harness.addToBattlefieldAndReturn(player1, new SkaabWrangler());
        Permanent creatureB = harness.addToBattlefieldAndReturn(player1, new SkaabWrangler());
        wrangler.setSummoningSick(true);
        creatureA.setSummoningSick(true);
        creatureB.setSummoningSick(true);
        Permanent target = addCreatureReady(player2, new SkaabWrangler());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(wrangler.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tappedWranglerCanActivateUsingThreeOtherCreatures() {
        Permanent wrangler = addCreatureReady(player1, new SkaabWrangler());
        wrangler.tap();
        Permanent creatureA = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureB = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureC = addCreatureReady(player1, new SkaabWrangler());
        Permanent target = addCreatureReady(player2, new SkaabWrangler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(creatureC.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tappedCreaturesAndOpponentsCreaturesCannotPayCost() {
        Permanent wrangler = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureA = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureB = addCreatureReady(player1, new SkaabWrangler());
        creatureB.tap();
        Permanent target = addCreatureReady(player2, new SkaabWrangler());
        addCreatureReady(player2, new SkaabWrangler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wrangler.isTapped()).isFalse();
        assertThat(creatureA.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canChooseThreeOtherCreaturesWithoutTappingWrangler() {
        Permanent wrangler = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureA = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureB = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureC = addCreatureReady(player1, new SkaabWrangler());
        Permanent target = addCreatureReady(player2, new SkaabWrangler());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handlePermanentChosen(player1, creatureA.getId());
        harness.handlePermanentChosen(player1, creatureB.getId());
        harness.handlePermanentChosen(player1, creatureC.getId());
        harness.passBothPriorities();

        assertThat(wrangler.isTapped()).isFalse();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(creatureC.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItselfWhileTappingItselfToPayCost() {
        Permanent wrangler = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureA = addCreatureReady(player1, new SkaabWrangler());
        Permanent creatureB = addCreatureReady(player1, new SkaabWrangler());

        harness.activateAbility(player1, 0, null, wrangler.getId());
        harness.passBothPriorities();

        assertThat(wrangler.isTapped()).isTrue();
        assertThat(creatureA.isTapped()).isTrue();
        assertThat(creatureB.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
