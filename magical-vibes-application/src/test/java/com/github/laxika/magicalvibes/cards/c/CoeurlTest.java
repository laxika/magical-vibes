package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BalefulEidolon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Coeurl.class, BalefulEidolon.class, GrizzlyBears.class, Plains.class})
class CoeurlTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W}, {T}: Taps target nonenchantment creature")
    void tapsTargetNonenchantmentCreature() {
        Permanent coeurl = addReadyCoeurl(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(coeurl.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target an enchantment creature")
    void cannotTargetEnchantmentCreature() {
        addReadyCoeurl(player1);
        Permanent target = addCreatureReady(player2, new BalefulEidolon());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addReadyCoeurl(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnCreature() {
        addReadyCoeurl(player1);
        Permanent target = addReadyCoeurl(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItself() {
        Permanent coeurl = addReadyCoeurl(player1);
        addActivationMana();

        harness.activateAbility(player1, 0, null, coeurl.getId());
        harness.passBothPriorities();

        assertThat(coeurl.isTapped()).isTrue();
    }

    @Test
    void canTargetAlreadyTappedCreature() {
        addReadyCoeurl(player1);
        Permanent target = addReadyCoeurl(player2);
        target.setTapped(true);
        addActivationMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        Permanent coeurl = harness.addToBattlefieldAndReturn(player1, new Coeurl());
        Permanent target = addReadyCoeurl(player2);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(coeurl.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent coeurl = addReadyCoeurl(player1);
        coeurl.setTapped(true);
        Permanent target = addReadyCoeurl(player2);
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotPayWhiteRequirementWithOnlyColorlessMana() {
        Permanent coeurl = addReadyCoeurl(player1);
        Permanent target = addReadyCoeurl(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(coeurl.isTapped()).isFalse();
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addReadyCoeurl(Player player) {
        return addCreatureReady(player, new Coeurl());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
