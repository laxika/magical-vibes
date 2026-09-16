package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisplacerKitten.class, GrizzlyBears.class, Island.class, Shock.class})
class DisplacerKittenTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell flickers a target nonland permanent you control")
    void noncreatureSpellFlickersTarget() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(target.getId());
    }

    @Test
    @DisplayName("The trigger can resolve without a target")
    void canChooseNoTarget() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castNoncreatureSpell();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger the ability")
    void creatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).singleElement()
                .extracting(entry -> entry.getEntryType())
                .isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("The trigger cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        castNoncreatureSpell();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The trigger cannot target an opponent's permanent")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new DisplacerKitten());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castNoncreatureSpell();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castNoncreatureSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
