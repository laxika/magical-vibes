package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.d.DarksteelMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OboshThePreypiercer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlameSpill.class, GrizzlyBears.class, ColossalDreadmaw.class,
        DarksteelMyr.class, OboshThePreypiercer.class})
class FlameSpillTest extends BaseCardTest {

    @Test
    void dealsExcessDamageToTargetCreatureController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void dealsNoExcessDamageWhenTargetNeedsAllFourDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new FlameSpill()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void redirectsExcessInsteadOfMarkingItOnIndestructibleCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Darksteel Myr");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 17);
    }

    @Test
    void redirectsAllDamageWhenIndestructibleCreatureAlreadyHasLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelMyr());
        target.setMarkedDamage(1);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 16);
    }

    @Test
    void determinesExcessBeforeApplyingOboshDamageMultiplier() {
        harness.addToBattlefield(player1, new OboshThePreypiercer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OboshThePreypiercer());
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Obosh, the Preypiercer");
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsExcessToOwnCreatureController() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OboshThePreypiercer());
        target.setMarkedDamage(4);
        harness.setHand(player1, List.of(new FlameSpill()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addMana();

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Obosh, the Preypiercer");
        harness.assertLife(player1, 14);
        harness.assertLife(player2, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
