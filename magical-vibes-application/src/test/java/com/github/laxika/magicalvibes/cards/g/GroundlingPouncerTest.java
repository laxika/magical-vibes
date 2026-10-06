package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({GroundlingPouncer.class, GrizzlyBears.class, SuntailHawk.class})
class GroundlingPouncerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate when no opponent controls a creature with flying")
    void cannotActivateWithoutOpponentFlyer() {
        addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls a creature with flying");
    }

    @Test
    @DisplayName("Resolving gives +1/+3 and flying when an opponent controls a flyer")
    void resolvingBoostsAndGrantsFlying() {
        Permanent pouncer = addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(3);   // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(4); // 1 + 3
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void onlyOncePerTurn() {
        addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent pouncer = addReadyPouncer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new SuntailHawk());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyPouncer(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GroundlingPouncer());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    void ownFlyerDoesNotPermitActivation() {
        addReadyPouncer(player1);
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls a creature with flying");
    }

    @Test
    void tappedSummoningSickPouncerCanActivate() {
        Permanent pouncer = harness.addToBattlefieldAndReturn(player1, new GroundlingPouncer());
        pouncer.setSummoningSick(true);
        pouncer.tap();
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();
        assertThat(pouncer.isTapped()).isTrue();
    }

    @Test
    void cannotActivateAgainWhileAbilityIsOnStack() {
        addReadyPouncer(player1);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    void resolvesAfterOpponentFlyerLeaves() {
        Permanent pouncer = addReadyPouncer(player1);
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player2.getId()).remove(flyer);
        gd.playerGraveyards.get(player2.getId()).add(flyer.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();
    }

    @Test
    void activationLimitIsPerPermanent() {
        Permanent first = addReadyPouncer(player1);
        Permanent second = addReadyPouncer(player1);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void activationLimitResetsOnOpponentsTurn() {
        Permanent pouncer = addReadyPouncer(player1);
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, pouncer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pouncer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, pouncer, Keyword.FLYING)).isTrue();
    }
}
