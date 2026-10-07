package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SterlingKeykeeper.class, TrainedArynx.class, Plains.class})
class SterlingKeykeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving ability taps target non-Mount creature")
    void resolvingAbilityTapsTargetNonMountCreature() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a Mount creature")
    void cannotTargetMountCreature() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent mount = addCreatureReady(player2, new TrainedArynx());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mount.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be a Mount");
    }

    @Test
    @DisplayName("Can target a creature controlled by the ability controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player1, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void paysTapCostBeforeTargetIsTapped() {
        Permanent keykeeper = addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(keykeeper.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();
        harness.passBothPriorities();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent keykeeper = addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keykeeper.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent keykeeper = addCreatureReady(player1, new SterlingKeykeeper());
        keykeeper.tap();
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTapTargetThatBecomesMountBeforeResolution() {
        addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        TestCards.mutableCard(target).setSubtypes(List.of(CardSubtype.MOUNT));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent keykeeper = addCreatureReady(player1, new SterlingKeykeeper());
        Permanent target = addCreatureReady(player2, new SterlingKeykeeper());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(keykeeper);
        gd.playerGraveyards.get(player1.getId()).add(keykeeper.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
