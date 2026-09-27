package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.cards.k.KagemarosClutch;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheCrescentMoon;
import com.github.laxika.magicalvibes.cards.m.MurmursFromBeyond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NikkoOnna.class, KagemarosClutch.class, KamiOfTheCrescentMoon.class,
        MurmursFromBeyond.class, InnerChamberGuard.class})
class NikkoOnnaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters by destroying a target enchantment")
    void entersByDestroyingTargetEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new KagemarosClutch());
        harness.setHand(player1, List.of(new NikkoOnna()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Kagemaro's Clutch");
        harness.assertOnBattlefield(player1, "Nikko-Onna");
    }

    @Test
    @DisplayName("Casting a Spirit spell may return Nikko-Onna to its owner's hand")
    void spiritSpellReturnsNikkoOnna() {
        addNikkoOnna();
        harness.castFromHand(player1, new KamiOfTheCrescentMoon(), "{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Nikko-Onna");
    }

    @Test
    @DisplayName("Casting an Arcane spell may return Nikko-Onna to its owner's hand")
    void arcaneSpellReturnsNikkoOnna() {
        addNikkoOnna();
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Nikko-Onna");
    }

    @Test
    @DisplayName("Declining the cast trigger leaves Nikko-Onna on the battlefield")
    void decliningCastTriggerLeavesNikkoOnnaOnBattlefield() {
        addNikkoOnna();
        harness.castFromHand(player1, new MurmursFromBeyond(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Nikko-Onna");
    }

    @Test
    @DisplayName("A non-Spirit non-Arcane spell does not trigger Nikko-Onna")
    void unrelatedSpellDoesNotTrigger() {
        addNikkoOnna();
        harness.castFromHand(player1, new InnerChamberGuard(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nikko-Onna");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ETB ability cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new InnerChamberGuard());
        harness.setHand(player1, List.of(new NikkoOnna()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can enter without an enchantment to target")
    void entersWithoutTargetWhenNoEnchantmentExists() {
        harness.addToBattlefield(player2, new InnerChamberGuard());

        harness.castFromHand(player1, new NikkoOnna(), "{2}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nikko-Onna");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Spirit spell does not trigger Nikko-Onna")
    void opponentSpiritSpellDoesNotTrigger() {
        harness.addToBattlefield(player2, new NikkoOnna());

        harness.castFromHand(player1, new KamiOfTheCrescentMoon(), "{U}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nikko-Onna");
        assertThat(gd.stack).isEmpty();
    }

    private void addNikkoOnna() {
        harness.addToBattlefield(player1, new NikkoOnna());
    }
}
