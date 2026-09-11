package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoggartBrute;
import com.github.laxika.magicalvibes.cards.f.FaerieMiscreant;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConcertedDefense.class, BoggartBrute.class, FaerieMiscreant.class, FugitiveWizard.class,
        GrizzlyBears.class, MightOfOaks.class, SoulWarden.class})
class ConcertedDefenseTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new ConcertedDefense()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a noncreature spell when its controller cannot pay {1}")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        MightOfOaks target = castMightOfOaks(4);
        castConcertedDefense(target.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    @Test
    @DisplayName("Adds one to the payment for each creature in the controller's party")
    void partyIncreasesPaymentCost() {
        addFullParty();
        MightOfOaks target = castMightOfOaks(9);
        castConcertedDefense(target.getId());

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInGraveyard(player1, "Might of Oaks");
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Might of Oaks");
    }

    private void addFullParty() {
        harness.addToBattlefield(player2, new SoulWarden());
        harness.addToBattlefield(player2, new FaerieMiscreant());
        harness.addToBattlefield(player2, new BoggartBrute());
        harness.addToBattlefield(player2, new FugitiveWizard());
    }

    private MightOfOaks castMightOfOaks(int mana) {
        var bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, mana);
        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        return might;
    }

    private void castConcertedDefense(java.util.UUID targetId) {
        harness.setHand(player2, List.of(new ConcertedDefense()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, targetId);
    }
}
