package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GenghisFrog;
import com.github.laxika.magicalvibes.cards.i.IceCreamKitty;
import com.github.laxika.magicalvibes.cards.f.FootNinjas;
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

@CardUsed({SplinterRadicalRat.class, FootNinjas.class, IceCreamKitty.class, GenghisFrog.class})
class SplinterRadicalRatTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles triggered abilities of Ninjas you control")
    void doublesOwnNinjaTriggeredAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        harness.setHand(player1, List.of(new FootNinjas()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(gd.getLife(player1.getId())).isEqualTo(26);
    }

    @Test
    @DisplayName("Does not double an opponent's Ninja triggered ability")
    void doesNotDoubleOpponentsNinjaTriggeredAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FootNinjas()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Makes a target Ninja unblockable and rejects non-Ninjas")
    void makesTargetNinjaUnblockable() {
        addCreatureReady(player1, new SplinterRadicalRat());
        Permanent ninja = addCreatureReady(player1, new FootNinjas());
        Permanent nonNinja = addCreatureReady(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonNinja.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, ninja.getId());
        harness.passBothPriorities();

        assertThat(ninja.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Can target an opponent's Ninja while Splinter is tapped")
    void targetsOpponentsNinjaWhileTapped() {
        Permanent splinter = addCreatureReady(player1, new SplinterRadicalRat());
        splinter.tap();
        Permanent ninja = addCreatureReady(player2, new FootNinjas());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, ninja.getId());
        harness.passBothPriorities();

        assertThat(ninja.isCantBeBlocked()).isTrue();
        assertThat(splinter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Unblockability expires when the turn ends")
    void unblockabilityExpiresAtEndOfTurn() {
        Permanent splinter = addCreatureReady(player1, new SplinterRadicalRat());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, splinter.getId());
        harness.passBothPriorities();
        assertThat(splinter.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);

        assertThat(splinter.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Does not double a non-Ninja's activated ability")
    void doesNotDoubleActivatedAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        addCreatureReady(player1, new IceCreamKitty());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
        harness.assertInGraveyard(player1, "Ice Cream Kitty");
    }

    @Test
    @DisplayName("Does not double a non-Ninja's triggered ability")
    void doesNotDoubleNonNinjaTriggeredAbility() {
        harness.addToBattlefield(player1, new SplinterRadicalRat());
        harness.addToBattlefield(player1, new GenghisFrog());
        harness.setHand(player1, List.of(new IceCreamKitty()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Mutagen")).isEqualTo(1);
    }
}
