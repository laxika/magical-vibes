package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.i.IceOver;
import com.github.laxika.magicalvibes.cards.i.ImplementOfImprovement;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PacificationArray.class, DruidOfTheCowl.class, ImplementOfImprovement.class, IceOver.class})
class PacificationArrayTest extends BaseCardTest {

    @Test
    void tapsTargetCreature() {
        harness.addToBattlefield(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapsTargetArtifact() {
        harness.addToBattlefield(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ImplementOfImprovement());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetEnchantment() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new IceOver());
        target.setAttachedTo(source.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    @Test
    void activationConsumesTwoManaAndTapsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(source.isTapped()).isTrue();
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void canActivateOnTheTurnItEnters() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        source.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTapOwnCreature() {
        harness.addToBattlefield(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        harness.addToBattlefield(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        target.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateTappedSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        source.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithOnlyOneMana() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(source.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new PacificationArray());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DruidOfTheCowl());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }
}
