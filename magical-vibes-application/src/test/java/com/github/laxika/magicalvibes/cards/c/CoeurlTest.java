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

    private Permanent addReadyCoeurl(Player player) {
        Permanent coeurl = new Permanent(new Coeurl());
        coeurl.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(coeurl);
        return coeurl;
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
