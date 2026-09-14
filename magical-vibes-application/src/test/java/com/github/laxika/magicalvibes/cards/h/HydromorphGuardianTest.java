package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.cards.c.CabalTorturer;
import com.github.laxika.magicalvibes.cards.f.FieryTemper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HydromorphGuardian.class, AvenTrooper.class, FieryTemper.class, CabalTorturer.class})
class HydromorphGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell targeting a creature you control")
    void countersSpellTargetingYourCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenTrooper());
        harness.addToBattlefield(player1, new HydromorphGuardian());

        FieryTemper fieryTemper = new FieryTemper();
        harness.setHand(player2, List.of(fieryTemper));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, target.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 1, null, fieryTemper.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fiery Temper");
        harness.assertOnBattlefield(player1, "Aven Trooper");
        harness.assertInGraveyard(player1, "Hydromorph Guardian");
    }

    @Test
    @DisplayName("Cannot target a spell that targets a creature you do not control")
    void cannotTargetSpellTargetingOpponentsCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AvenTrooper());
        harness.addToBattlefield(player1, new HydromorphGuardian());

        FieryTemper fieryTemper = new FieryTemper();
        harness.setHand(player1, List.of(fieryTemper));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, target.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fieryTemper.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a spell that targets a player")
    void cannotTargetSpellTargetingPlayer() {
        harness.addToBattlefield(player1, new HydromorphGuardian());

        FieryTemper fieryTemper = new FieryTemper();
        harness.setHand(player2, List.of(fieryTemper));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fieryTemper.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an activated ability even when it targets a creature you control")
    void cannotTargetActivatedAbility() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AvenTrooper());
        harness.addToBattlefield(player1, new HydromorphGuardian());
        addCreatureReady(player2, new CabalTorturer());
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, target.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 1, null, gd.stack.getLast().getTargetableId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
