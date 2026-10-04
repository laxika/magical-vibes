package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.i.Ixidron;
import com.github.laxika.magicalvibes.cards.l.Lignify;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThousandYearElixir;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaddockTeeg.class, WrathOfGod.class, Hurricane.class, Shock.class,
        GrizzlyBears.class, HillGiant.class, GarrukWildspeaker.class, ThousandYearElixir.class,
        Lignify.class, Ixidron.class})
class GaddockTeegTest extends BaseCardTest {

    @Test
    @DisplayName("Noncreature spell with mana value 4 or greater can't be cast")
    void blocksHighManaValueNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spell with {X} in its cost can't be cast even at low mana value")
    void blocksXNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new Hurricane())); // {X}{G} sorcery — MV 1 at X=0, but has {X}
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Noncreature spell with mana value 3 or less is still castable")
    void allowsLowManaValueNoncreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player1, List.of(new Shock())); // {R} instant, MV 1
        harness.addMana(player1, ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Creature spell with mana value 4 or greater is still castable")
    void allowsHighManaValueCreatureSpell() {
        harness.addToBattlefield(player1, new GaddockTeeg());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Restriction is symmetric — the opponent also can't cast high-MV noncreature spells")
    void restrictionAppliesToOpponent() {
        harness.addToBattlefield(player1, new GaddockTeeg());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Removing Gaddock Teeg restores casting of high-MV noncreature spells")
    void removingGaddockTeegRestoresCasting() {
        harness.addToBattlefield(player1, new GaddockTeeg());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Gaddock Teeg"));

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
    }

    @Test
    @DisplayName("A planeswalker with mana value four cannot be cast")
    void blocksPlaneswalkerAtManaValueFour() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castFromHand(player1, new GarrukWildspeaker(), "{2}{G}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("An artifact with mana value three can be cast")
    void allowsArtifactAtManaValueThree() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new ThousandYearElixir(), "{3}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thousand-Year Elixir");
    }

    @Test
    @DisplayName("The opponent cannot cast an X spell even when X is zero")
    void blocksOpponentsXSpellAtZero() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        harness.setHand(player2, List.of(new Hurricane()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Lignify removes Gaddock Teeg's casting restrictions")
    void losingAbilitiesRestoresCasting() {
        harness.addToBattlefield(player1, new GaddockTeeg());
        UUID teegId = harness.getPermanentId(player1, "Gaddock Teeg");
        harness.setHand(player1, List.of(new Lignify()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, teegId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Lignify");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gaddock Teeg");
        harness.assertInGraveyard(player1, "Wrath of God");
    }

    @Test
    @DisplayName("Face-down Gaddock Teeg does not restrict high mana value spells")
    void turningFaceDownRestoresHighManaValueCasting() {
        turnGaddockTeegFaceDown();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Wrath of God");
    }

    @Test
    @DisplayName("Face-down Gaddock Teeg does not restrict X spells")
    void turningFaceDownRestoresXCasting() {
        turnGaddockTeegFaceDown();
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Hurricane");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void turnGaddockTeegFaceDown() {
        var teeg = harness.addToBattlefieldAndReturn(player1, new GaddockTeeg());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new Ixidron(), "{3}{U}{U}");
        harness.passBothPriorities();

        assertThat(teeg.isFaceDown()).isTrue();
        harness.assertOnBattlefield(player1, "Ixidron");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
