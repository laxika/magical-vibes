package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.Dreamcatcher;
import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.i.IvoryCraneNetsuke;
import com.github.laxika.magicalvibes.cards.s.SpiritualVisit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KiriOnna.class, Dreamcatcher.class, SpiritualVisit.class,
        InnerChamberGuard.class, IvoryCraneNetsuke.class})
class KiriOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by returning a target creature to its owner's hand")
    void entersByReturningTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InnerChamberGuard());
        harness.setHand(player1, List.of(new KiriOnna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Inner-Chamber Guard");
        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Kiri-Onna to its owner's hand")
    void spiritSpellReturnsKiriOnna() {
        addKiriOnna();
        harness.castFromHand(player1, new Dreamcatcher(), "{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Kiri-Onna to its owner's hand")
    void arcaneSpellReturnsKiriOnna() {
        addKiriOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Kiri-Onna on the battlefield")
    void decliningCastTriggerLeavesKiriOnnaOnBattlefield() {
        addKiriOnna();
        harness.castFromHand(player1, new SpiritualVisit(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Kiri-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addKiriOnna();
        harness.castFromHand(player1, new InnerChamberGuard(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Kiri-Onna")
    void opponentsSpiritSpellDoesNotTrigger() {
        addKiriOnna();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Dreamcatcher(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Kiri-Onna");
    }

    @Test
    @DisplayName("The ETB ability cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2,
                new IvoryCraneNetsuke());
        harness.setHand(player1, List.of(new KiriOnna()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addKiriOnna() {
        return harness.addToBattlefieldAndReturn(player1, new KiriOnna());
    }
}
