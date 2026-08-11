package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShrivelingRotTest extends BaseCardTest {

    @Test
    @DisplayName("Damage mode destroys a creature that survives the damage")
    void damageModeDestroysCreature() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new ShrivelingRot(), new Shock()));
        addMana(false);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, List.of());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Death mode uses the dying creature's toughness")
    void deathModeUsesToughness() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ShrivelingRot()));
        addMana(false);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{1}, List.of());
        harness.passBothPriorities();

        spider.setMarkedDamage(4);
        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Entwine registers both global triggers")
    void entwineRegistersBothTriggers() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new ShrivelingRot(), new Shock()));
        addMana(true);

        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of());
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Entwine requires its additional mana")
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new ShrivelingRot()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castModalInstantWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(boolean entwined) {
        harness.addMana(player1, ManaColor.BLACK, entwined ? 3 : 2);
        harness.addMana(player1, ManaColor.COLORLESS, entwined ? 4 : 2);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
