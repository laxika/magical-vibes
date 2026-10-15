package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.c.CurseOfExhaustion;
import com.github.laxika.magicalvibes.cards.e.ElvishVisionary;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindclawShaman.class, Divination.class, ElvishVisionary.class, Naturalize.class,
        SearingSpear.class, ThaliaGuardianOfThraben.class, CurseOfExhaustion.class})
class MindclawShamanTest extends BaseCardTest {

    private void castShaman() {
        harness.setHand(player1, new ArrayList<>(List.of(new MindclawShaman())));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities(); // creature resolves -> ETB trigger on stack
        harness.passBothPriorities(); // ETB trigger resolves
    }

    @Test
    @DisplayName("ETB lets the controller cast an instant/sorcery from the opponent's hand for free")
    void castsOpponentSorceryForFree() {
        Divination stolen = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(stolen)));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(stolen.getId());
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(stolen.getId()));
    }

    @Test
    @DisplayName("Declining leaves the spell in the opponent's hand")
    void decliningLeavesSpellInHand() {
        Divination stolen = new Divination();
        harness.setHand(player2, new ArrayList<>(List.of(stolen)));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(stolen.getId()));
    }

    @Test
    @DisplayName("Non-instant/sorcery cards in the opponent's hand are not offered")
    void creatureIsNotOffered() {
        harness.setHand(player2, new ArrayList<>(List.of(new ElvishVisionary())));

        castShaman();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Empty opponent hand offers nothing")
    void emptyHandOffersNothing() {
        harness.setHand(player2, new ArrayList<>());

        castShaman();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The stolen sorcery draws for its caster and goes to its owner's graveyard")
    void stolenSorceryResolvesForCaster() {
        Divination stolen = new Divination();
        ElvishVisionary first = new ElvishVisionary();
        ElvishVisionary second = new ElvishVisionary();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player2, List.of(stolen));

        castShaman();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stolen);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(stolen);
    }

    @Test
    @DisplayName("The caster chooses the stolen instant's target")
    void castsTargetedInstant() {
        SearingSpear stolen = new SearingSpear();
        harness.setHand(player2, List.of(stolen));
        harness.setLife(player2, 20);

        castShaman();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(stolen);
    }

    @Test
    @DisplayName("Declining the first spell allows choosing another, but only one is cast")
    void choosesOnlyOneOfSeveralSpells() {
        Divination first = new Divination();
        Divination second = new Divination();
        Divination third = new Divination();
        harness.setHand(player2, List.of(first, second, third));

        castShaman();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(second);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An instant without a legal target stays in the opponent's hand")
    void spellWithoutLegalTargetsStaysInHand() {
        Naturalize stolen = new Naturalize();
        harness.setHand(player2, List.of(stolen));

        castShaman();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen);
    }

    @Test
    @DisplayName("Without paying the mana cost does not waive Thalia's cost increase")
    void cannotCastWhenCostIncreaseCannotBePaid() {
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        Divination stolen = new Divination();
        harness.setHand(player2, List.of(stolen));

        castShaman();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen);
    }

    @Test
    @DisplayName("The free spell cannot bypass a one-spell-per-turn restriction")
    void cannotCastUnderCurseOfExhaustionAfterCastingShaman() {
        harness.addToBattlefieldAndReturn(player2, new CurseOfExhaustion())
                .setAttachedTo(player1.getId());
        Divination stolen = new Divination();
        harness.setHand(player2, List.of(stolen));

        castShaman();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(stolen);
    }
}
