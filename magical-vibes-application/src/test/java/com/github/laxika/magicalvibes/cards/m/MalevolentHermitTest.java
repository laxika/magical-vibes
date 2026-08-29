package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BenevolentGeist;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MalevolentHermit.class, BenevolentGeist.class, Counterspell.class, GrizzlyBears.class, Shock.class})
class MalevolentHermitTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifice ability counters an unpayable noncreature spell")
    void sacrificeAbilityCountersUnpayableNoncreatureSpell() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new MalevolentHermit());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hermit.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("Sacrifice ability leaves the spell on the stack when its controller pays")
    void sacrificeAbilityLeavesSpellWhenControllerPays() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new MalevolentHermit());
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.RED, 4);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);
        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(hermit.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
    }

    @Test
    @DisplayName("Sacrifice ability cannot target a creature spell")
    void sacrificeAbilityCannotTargetCreatureSpell() {
        Permanent hermit = harness.addToBattlefieldAndReturn(player1, new MalevolentHermit());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hermit);
    }

    @Test
    @DisplayName("Disturb puts the transformed back face onto the battlefield")
    void disturbPutsBackFaceOntoBattlefield() {
        Permanent geist = putGeistOnBattlefield();

        assertThat(geist.isTransformed()).isTrue();
        assertThat(geist.getCard()).isInstanceOf(BenevolentGeist.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Benevolent Geist protects your noncreature spells but not creature spells")
    void protectsNoncreatureSpellsOnly() {
        putGeistOnBattlefield();

        Shock shock = new Shock();
        Counterspell counterspell = new Counterspell();
        harness.setHand(player1, List.of(shock));
        harness.setHand(player2, List.of(counterspell));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(counterspell);

        GrizzlyBears bears = new GrizzlyBears();
        Counterspell secondCounterspell = new Counterspell();
        harness.setHand(player1, List.of(bears));
        harness.setHand(player2, List.of(secondCounterspell));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
    }

    @Test
    @DisplayName("Benevolent Geist is exiled instead of going to the graveyard")
    void backFaceIsExiledInsteadOfGoingToGraveyard() {
        Permanent geist = putGeistOnBattlefield();
        UUID cardId = geist.getOriginalCard().getId();

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, geist));

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId())).contains(cardId);
    }

    private Permanent putGeistOnBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        MalevolentHermit hermit = new MalevolentHermit();
        harness.setGraveyard(player1, List.of(hermit));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
