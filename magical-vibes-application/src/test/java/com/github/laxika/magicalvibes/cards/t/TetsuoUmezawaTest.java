package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ChainLightning;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.cards.k.Karakas;
import com.github.laxika.magicalvibes.cards.s.SpiritLink;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TetsuoUmezawa.class, DurkwoodBoars.class, SpiritLink.class, Karakas.class, ChainLightning.class})
class TetsuoUmezawaTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature")
    void destroysTappedCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Destroys a blocking creature")
    void destroysBlockingCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());
        target.setBlocking(true);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Cannot target an untapped nonblocking creature")
    void rejectsUntappedNonblockingCreature() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void rejectsTappedNoncreaturePermanent() {
        readyTetsuo();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Karakas());
        target.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not destroy a creature that becomes untapped before resolution")
    void targetMustRemainTappedAtResolution() {
        readyTetsuo();
        Permanent target = addCreatureReady(player2, new DurkwoodBoars());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Durkwood Boars");
        harness.assertNotInGraveyard(player2, "Durkwood Boars");
    }

    @Test
    @DisplayName("Non-Aura spells can target Tetsuo Umezawa")
    void nonAuraSpellsCanTargetTetsuoUmezawa() {
        Permanent tetsuo = addCreatureReady(player2, new TetsuoUmezawa());
        harness.setHand(player1, List.of(new ChainLightning()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, tetsuo.getId());

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof ChainLightning);
    }

    @Test
    @DisplayName("Aura spells cannot target Tetsuo Umezawa")
    void auraSpellsCannotTargetTetsuoUmezawa() {
        Permanent tetsuo = addCreatureReady(player2, new TetsuoUmezawa());
        harness.setHand(player1, List.of(new SpiritLink()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, tetsuo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be enchanted by other Auras");
    }

    private void readyTetsuo() {
        addCreatureReady(player1, new TetsuoUmezawa());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
