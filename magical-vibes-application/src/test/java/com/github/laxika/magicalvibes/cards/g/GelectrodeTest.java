package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Gelectrode.class, Shock.class, MindRot.class})
class GelectrodeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Gelectrode deals 1 damage to a target player")
    void tappingDealsDamageToPlayer() {
        Permanent gelectrode = addReadyGelectrode();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting an instant may untap Gelectrode")
    void instantSpellMayUntap() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveStack();

        assertThat(gelectrode.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Casting a sorcery may untap Gelectrode")
    void sorcerySpellMayUntap() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        resolveStack();

        assertThat(gelectrode.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the trigger leaves Gelectrode tapped")
    void decliningUntapLeavesItTapped() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, false);
        resolveStack();

        assertThat(gelectrode.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Casting a non-instant, non-sorcery spell does not trigger Gelectrode")
    void creatureSpellDoesNotTrigger() {
        Permanent gelectrode = addTappedGelectrode();
        harness.setHand(player1, List.of(new Gelectrode()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gelectrode.isTapped()).isTrue();
    }

    private Permanent addReadyGelectrode() {
        Permanent gelectrode = harness.addToBattlefieldAndReturn(player1, new Gelectrode());
        gelectrode.setSummoningSick(false);
        return gelectrode;
    }

    private Permanent addTappedGelectrode() {
        Permanent gelectrode = addReadyGelectrode();
        gelectrode.tap();
        return gelectrode;
    }

    private void resolveStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
