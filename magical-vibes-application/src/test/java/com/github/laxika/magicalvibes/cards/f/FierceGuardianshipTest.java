package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FierceGuardianship.class, GrizzlyBears.class, MightOfOaks.class})
class FierceGuardianshipTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for free while its controller controls a commander")
    void castsForFreeWithCommander() {
        Permanent commander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        commander.setCommander(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks spell = new MightOfOaks();

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new FierceGuardianship()));
        harness.castInstantWithAlternateCost(player2, 0, spell.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player2, "Fierce Guardianship");
    }

    @Test
    @DisplayName("Cannot use the free cast without controlling a commander")
    void cannotCastForFreeWithoutCommander() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks spell = new MightOfOaks();

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new FierceGuardianship()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player2, 0, spell.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears spell = new GrizzlyBears();
        Permanent commander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        commander.setCommander(true);

        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new FierceGuardianship()));

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player2, 0, spell.getId(), List.of()))
                .isInstanceOf(IllegalStateException.class);
    }
}
