package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KavuRecluse;
import com.github.laxika.magicalvibes.cards.s.StarCompass;
import com.github.laxika.magicalvibes.cards.s.StoneKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DromarsCharm.class, KavuRecluse.class, StarCompass.class, StoneKavu.class})
class DromarsCharmTest extends BaseCardTest {

    @Test
    @DisplayName("Mode 0 makes its controller gain 5 life")
    void gainsFiveLife() {
        harness.setLife(player1, 10);
        harness.castFromHand(player1, new DromarsCharm(), "{W}{U}{B}");

        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Mode 1 counters a target spell")
    void countersSpell() {
        DromarsCharm spell = new DromarsCharm();
        harness.castFromHand(player1, spell, "{W}{U}{B}");
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new DromarsCharm()));
        addMana(player2);

        harness.castInstant(player2, 0, 1, spell.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dromar's Charm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mode 1 cannot target a nonspell permanent")
    void counterModeRejectsPermanentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StarCompass());
        harness.setHand(player2, List.of(new DromarsCharm()));
        addMana(player2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, 1, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("spell");
    }

    @Test
    @DisplayName("Mode 2 gives a target creature -2/-2 until end of turn")
    void weakensCreatureUntilEndOfTurn() {
        Permanent target = addCreatureReady(player2, new StoneKavu());
        harness.setHand(player1, List.of(new DromarsCharm()));
        addMana(player1);

        harness.castInstant(player1, 0, 2, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Mode 2 puts a creature with zero toughness into its owner's graveyard")
    void weakeningKillsCreatureWithZeroToughness() {
        harness.addToBattlefield(player2, new KavuRecluse());
        harness.setHand(player1, List.of(new DromarsCharm()));
        addMana(player1);

        UUID targetId = harness.getPermanentId(player2, "Kavu Recluse");
        harness.castInstant(player1, 0, 2, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Kavu Recluse");
        harness.assertInGraveyard(player2, "Kavu Recluse");
    }

    @Test
    @DisplayName("Mode 2 cannot target a noncreature permanent")
    void modeTwoRejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StarCompass());
        harness.setHand(player1, List.of(new DromarsCharm()));
        addMana(player1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
    }

}
