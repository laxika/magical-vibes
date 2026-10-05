package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DarkthicketWolf;
import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.d.DevilsPlay;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mindshrieker.class, Forest.class, DarkthicketWolf.class, AbbeyGriffin.class, DevilsPlay.class, Geistflame.class, BruvacTheGrandiloquent.class})
class MindshriekerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting a player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addMindshrieker(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(Mindshrieker.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addMindshrieker(player1);
        addActivationMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mills one card and boosts by milled card's mana value")
    void millsOneCardAndBoosts() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Set up opponent library with a known card: AbbeyGriffin (MV 4)
        harness.setLibrary(player2, List.of(new AbbeyGriffin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Mindshrieker should get +4/+4 (base 1/1 + modifier 4/4)
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(4);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(4);
        // Opponent's library should be empty
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        // Milled card should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost is zero when milled card has mana value 0")
    void boostIsZeroForLand() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Forest has MV 0
        harness.setLibrary(player2, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(0);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Boost equals exact mana value of milled card")
    void boostEqualsExactManaValue() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // DarkthicketWolf has MV 2
        harness.setLibrary(player2, List.of(new DarkthicketWolf()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(2);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target yourself to mill your own library")
    void canTargetSelf() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        // Set up own library with DevilsPlay (MV 1)
        harness.setLibrary(player1, List.of(new DevilsPlay()));

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(1);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple activations stack boosts")
    void multipleActivationsStackBoosts() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);
        addActivationMana(player1);

        // DarkthicketWolf (MV 2) + DevilsPlay (MV 1)
        harness.setLibrary(player2, List.of(
                new DarkthicketWolf(), new DevilsPlay()
        ));

        // First activation mills DarkthicketWolf (MV 2)
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Second activation mills DevilsPlay (MV 1)
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Total boost: +2/+2 + +1/+1 = +3/+3
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(3);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when target player's library is empty")
    void doesNothingWhenLibraryEmpty() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);

        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(mindshrieker.getPowerModifier()).isEqualTo(0);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mills only one card even when library has many")
    void millsOnlyOneCard() {
        addMindshrieker(player1);
        addActivationMana(player1);

        harness.setLibrary(player2, List.of(
                new DevilsPlay(), new DarkthicketWolf(), new AbbeyGriffin()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Only 1 card milled, 2 remain
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void doubledMillUsesTotalManaValue() {
        Permanent mindshrieker = addMindshrieker(player1);
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        addActivationMana(player1);
        harness.setLibrary(player2, List.of(new DarkthicketWolf(), new AbbeyGriffin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(6);
        assertThat(mindshrieker.getToughnessModifier()).isEqualTo(6);
    }

    @Test
    void boostExpiresAtEndOfTurn() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);
        harness.setLibrary(player2, List.of(new AbbeyGriffin()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(mindshrieker.getPowerModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(mindshrieker.getPowerModifier()).isZero();
        assertThat(mindshrieker.getToughnessModifier()).isZero();
    }

    @Test
    void millsEvenWhenSourceIsDestroyedInResponse() {
        Permanent mindshrieker = addMindshrieker(player1);
        addActivationMana(player1);
        harness.setLibrary(player2, List.of(new AbbeyGriffin()));
        harness.setHand(player2, List.of(new Geistflame()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castInstant(player2, 0, mindshrieker.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Mindshrieker");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Abbey Griffin");
    }

    private Permanent addMindshrieker(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new Mindshrieker());
        perm.setSummoningSick(false);
        return perm;
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
