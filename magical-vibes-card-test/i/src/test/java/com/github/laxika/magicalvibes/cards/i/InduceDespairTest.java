package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LagacLizard;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.cards.s.Swamp;
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

@CardUsed({InduceDespair.class, NestInvader.class, LagacLizard.class, Swamp.class})
class InduceDespairTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a creature -X/-X based on the revealed creature card's mana value")
    void usesRevealedCreatureManaValue() {
        Permanent target = addCreatureReady(player2, new LagacLizard());
        InduceDespair spell = new InduceDespair();
        NestInvader revealed = new NestInvader();
        harness.setHand(player1, List.of(spell, revealed));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("The -X/-X effect wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent target = addCreatureReady(player2, new LagacLizard());
        harness.setHand(player1, List.of(new InduceDespair(), new NestInvader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature dies when the revealed four-mana creature reduces its toughness below zero")
    void usesFourManaValue() {
        Permanent target = addCreatureReady(player2, new NestInvader());
        harness.setHand(player1, List.of(new InduceDespair(), new LagacLizard()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nest Invader");
        harness.assertInGraveyard(player2, "Nest Invader");
    }

    @Test
    @DisplayName("Cannot be cast without a creature card to reveal")
    void requiresCreatureCardToReveal() {
        Permanent target = addCreatureReady(player2, new LagacLizard());
        harness.setHand(player1, List.of(new InduceDespair(), new Swamp()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Revealed card must be creature card");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new InduceDespair(), new NestInvader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature reveal is mandatory even when no card is selected")
    void cannotSkipReveal() {
        Permanent target = addCreatureReady(player2, new LagacLizard());
        harness.setHand(player1, List.of(new InduceDespair(), new NestInvader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can reveal a creature before the spell's hand index and target your own creature")
    void revealsEarlierHandCardAndTargetsOwnCreature() {
        Permanent target = addCreatureReady(player1, new LagacLizard());
        NestInvader revealed = new NestInvader();
        harness.setHand(player1, List.of(revealed, new InduceDespair()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 1, target.getId(), 0);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The revealed card need not remain in hand until resolution")
    void remembersRevealedManaValueAfterHandChanges() {
        Permanent target = addCreatureReady(player2, new LagacLizard());
        harness.setHand(player1, List.of(new InduceDespair(), new NestInvader()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.setHand(player1, List.of(new LagacLizard()));
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature reduced to exactly zero toughness dies")
    void killsAtExactlyZeroToughness() {
        Permanent target = addCreatureReady(player2, new NestInvader());
        NestInvader revealed = new NestInvader();
        harness.setHand(player1, List.of(new InduceDespair(), revealed));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Nest Invader");
        harness.assertInGraveyard(player2, "Nest Invader");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }
}
