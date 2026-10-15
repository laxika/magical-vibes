package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BlackPoplarShaman;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThorntoothWitch.class, BlackPoplarShaman.class, GiantSpider.class,
        GrizzlyBears.class, NamelessInversion.class})
class ThorntoothWitchTest extends BaseCardTest {

    /** Casts Black Poplar Shaman (a Treefolk spell) from player1's hand. */
    private void castTreefolkSpell() {
        harness.castFromHand(player1, new BlackPoplarShaman(), "{2}{B}");
    }

    @Test
    @DisplayName("Casting a Treefolk spell requires a target before the resolution choice")
    void treefolkSpellTriggers() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castTreefolkSpell();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting gives target creature +3/-3 until end of turn")
    void acceptBuffsAndDebuffsTarget() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castTreefolkSpell();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(spider.getPowerModifier()).isEqualTo(3);
        assertThat(spider.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("+3/-3 kills a 2/2 creature")
    void debuffKillsSmallCreature() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castTreefolkSpell();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining leaves the target unchanged")
    void declineLeavesTarget() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        castTreefolkSpell();
        harness.handlePermanentChosen(player1, spider.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(spider.getPowerModifier()).isEqualTo(0);
        assertThat(spider.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Casting a non-Treefolk spell does not trigger")
    void nonTreefolkSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's Treefolk spell does not trigger the Witch")
    void opponentsTreefolkDoesNotTrigger() {
        harness.addToBattlefield(player1, new ThorntoothWitch());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new BlackPoplarShaman(), "{2}{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting the Witch itself does not trigger its own ability")
    void witchDoesNotTriggerFromItsOwnCast() {
        harness.castFromHand(player1, new ThorntoothWitch(), "{5}{B}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Thorntooth Witch");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The Witch can target itself and the modifiers expire at cleanup")
    void canTargetItselfUntilEndOfTurn() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new ThorntoothWitch());

        castTreefolkSpell();
        harness.handlePermanentChosen(player1, witch.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(witch.getPowerModifier()).isEqualTo(3);
        assertThat(witch.getToughnessModifier()).isEqualTo(-3);
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player1, TurnStep.CLEANUP);
        assertThat(witch.getPowerModifier()).isZero();
        assertThat(witch.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("A noncreature spell with changeling is also a Treefolk spell")
    void kindredChangelingSpellTriggers() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new ThorntoothWitch());
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new NamelessInversion()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Giant Spider"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, witch.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(witch.getPowerModifier()).isEqualTo(3);
        assertThat(witch.getToughnessModifier()).isEqualTo(-3);
    }
}
