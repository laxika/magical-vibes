package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WardscaleDragon;
import com.github.laxika.magicalvibes.cards.h.HerosBlade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefiantOgre.class, WardscaleDragon.class, HerosBlade.class})
class DefiantOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Counter mode puts a +1/+1 counter on Defiant Ogre")
    void counterModePutsCounterOnItself() {
        castOgre(0, null);

        Permanent ogre = findPermanent(player1, "Defiant Ogre");
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ogre)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ogre)).isEqualTo(6);
    }

    @Test
    @DisplayName("Destroy mode destroys the target artifact")
    void destroyModeDestroysArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HerosBlade());

        castOgre(1, target.getId());

        harness.assertNotOnBattlefield(player2, "Hero's Blade");
        harness.assertInGraveyard(player2, "Hero's Blade");
    }

    @Test
    @DisplayName("Destroy mode rejects a nonartifact target")
    void destroyModeRejectsNonartifactTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WardscaleDragon());

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HerosBlade());
        harness.castFromHand(player1, new DefiantOgre(), "{5}{R}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Destroy target artifact");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Wardscale Dragon");
        harness.assertInGraveyard(player2, "Hero's Blade");
    }

    @Test
    @DisplayName("Mode and artifact target are chosen after the creature spell resolves")
    void modeAndTargetAreChosenWhenOgreEnters() {
        harness.castFromHand(player1, new DefiantOgre(), "{5}{R}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new HerosBlade());
        harness.passBothPriorities();

        Permanent ogre = findPermanent(player1, "Defiant Ogre");
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "Destroy target artifact");
        harness.handlePermanentChosen(player1, artifact.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hero's Blade");
        assertThat(ogre.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Destroy mode can target an artifact controlled by the Ogre's controller")
    void destroyModeCanDestroyOwnArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new HerosBlade());

        castOgre(1, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Hero's Blade");
        harness.assertInGraveyard(player1, "Hero's Blade");
        assertThat(findPermanent(player1, "Defiant Ogre")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castOgre(int mode, java.util.UUID targetId) {
        harness.castFromHand(player1, new DefiantOgre(), "{5}{R}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode == 0
                ? "Put a +1/+1 counter on this creature" : "Destroy target artifact");
        if (targetId != null) {
            harness.handlePermanentChosen(player1, targetId);
        }
        resolveAllTriggers();
    }
}
