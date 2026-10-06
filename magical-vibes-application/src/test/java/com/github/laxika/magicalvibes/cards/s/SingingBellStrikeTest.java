package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SingingBellStrike.class, AlpineGrizzly.class, Forest.class, SaguMauler.class})
class SingingBellStrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Singing Bell Strike taps and attaches to the target creature")
    void entersTappedAndAttached() {
        Permanent bears = addCreatureReady(player2, new AlpineGrizzly());

        harness.setHand(player1, List.of(new SingingBellStrike()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Singing Bell Strike")
                        && permanent.isAttached()
                        && bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Singing Bell Strike prevents the enchanted creature from untapping")
    void enchantedCreatureDoesNotUntap() {
        Permanent bears = addCreatureReady(player2, new AlpineGrizzly());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SingingBellStrike());
        aura.setAttachedTo(bears.getId());

        harness.performUntapStep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature can pay {6} to untap itself")
    void enchantedCreatureCanUntapForSixMana() {
        Permanent bears = addCreatureReady(player1, new AlpineGrizzly());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SingingBellStrike());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Singing Bell Strike cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SingingBellStrike()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        Permanent forest = findPermanent(player1, "Forest");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void enterTriggerStillTapsCreatureThatGainsHexproof() {
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new SaguMauler()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        resolveAllTriggers();
        Permanent mauler = findPermanent(player2, "Sagu Mauler");

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new SingingBellStrike()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, mauler.getId());
        harness.passBothPriorities();
        assertThat(mauler.isTapped()).isFalse();

        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.turnFaceUp(player2, 0);
        resolveAllTriggers();

        assertThat(mauler.isTapped()).isTrue();
        assertThat(findPermanent(player1, "Singing Bell Strike").getAttachedTo())
                .isEqualTo(mauler.getId());
    }

    @Test
    void opposingCreatureControllerCanPayToUntapWhileSummoningSick() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SingingBellStrike());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 6);

        harness.activateAbility(player2, 0, null, null);
        assertThat(creature.isTapped()).isTrue();
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void fiveManaCannotPayForGrantedUntapAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SingingBellStrike());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void removingAuraRestoresNormalUntappingButDoesNotStopPendingAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AlpineGrizzly());
        creature.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SingingBellStrike());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 6);
        harness.activateAbility(player2, 0, null, null);

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);
        resolveAllTriggers();
        assertThat(creature.isTapped()).isFalse();

        creature.tap();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
