package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({NicoMinoruRunaway.class, Forest.class, GrizzlyBears.class, Pyroclasm.class, Shock.class})
class NicoMinoruRunawayTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell from hand does not trigger Nico")
    void castingFromHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new NicoMinoruRunaway());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The activated ability discards, exiles until a nonland, and offers it for free")
    void activatedAbilityCastsExiledNonlandForFree() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);

        GrizzlyBears discarded = new GrizzlyBears();
        Forest land = new Forest();
        Pyroclasm spell = new Pyroclasm();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(land, spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land, spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Pyroclasm");
    }

    @Test
    @DisplayName("The activated ability cannot be paid without a discard")
    void activatedAbilityRequiresDiscard() {
        forceMainPhase();
        Permanent nico = harness.addToBattlefieldAndReturn(player1, new NicoMinoruRunaway());
        nico.setSummoningSick(false);
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
