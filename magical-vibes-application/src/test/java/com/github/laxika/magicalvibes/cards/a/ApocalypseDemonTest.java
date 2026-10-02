package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DefiantKhenra;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApocalypseDemon.class, DefiantKhenra.class, Plains.class, Abrade.class})
class ApocalypseDemonTest extends BaseCardTest {

    @Test
    @DisplayName("P/T equals the number of cards in the controller's graveyard, of any type")
    void ptEqualsCardsInOwnGraveyard() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ApocalypseDemon());

        List<Card> graveyard = new ArrayList<>();
        graveyard.add(new DefiantKhenra());
        graveyard.add(new DefiantKhenra());
        graveyard.add(new Plains());
        graveyard.add(new Abrade());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    @Test
    @DisplayName("P/T counts only the controller's graveyard, not the opponent's")
    void ptCountsOnlyControllerGraveyard() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ApocalypseDemon());
        harness.setGraveyard(player1, createCards(3));
        harness.setGraveyard(player2, createCards(5));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the sacrifice taps the Demon")
    void decliningTapsDemon() {
        harness.setGraveyard(player1, createCards(3)); // Demon is 3/3, survives SBA
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new DefiantKhenra());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(demon(player1).isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Defiant Khenra");
    }

    @Test
    @DisplayName("Sacrificing another creature leaves the Demon untapped")
    void sacrificingLeavesDemonUntapped() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new DefiantKhenra());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Defiant Khenra");
        assertThat(demon(player1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("With no other creature to sacrifice, the Demon is tapped without a prompt")
    void noOtherCreatureTapsDemon() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(demon(player1).isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new DefiantKhenra());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(demon(player1).isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Defiant Khenra");
    }

    @Test
    @DisplayName("Power and toughness update as cards enter and leave the graveyard")
    void ptUpdatesWithGraveyardChanges() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ApocalypseDemon());
        harness.setGraveyard(player1, createCards(2));
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);

        harness.setGraveyard(player1, createCards(5));
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);

        harness.setGraveyard(player1, List.of());
        assertThat(gqs.getEffectivePower(gd, perm)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, perm)).isZero();
    }

    @Test
    @DisplayName("An opponent's creature and your noncreature cannot pay the upkeep cost")
    void cannotSacrificeOpponentCreatureOrOwnLand() {
        harness.setGraveyard(player1, createCards(3));
        harness.addToBattlefield(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new DefiantKhenra());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(demon(player1).isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Plains");
        harness.assertOnBattlefield(player2, "Defiant Khenra");
    }

    @Test
    @DisplayName("Paying the upkeep cost does not untap an already tapped Demon")
    void sacrificingDoesNotUntapDemon() {
        harness.setGraveyard(player1, createCards(3));
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ApocalypseDemon());
        harness.addToBattlefield(player1, new DefiantKhenra());

        advanceToUpkeep(player1);
        perm.setTapped(true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(perm.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Defiant Khenra");
        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(4);
    }

    private Permanent demon(Player owner) {
        UUID id = harness.getPermanentId(owner, "Apocalypse Demon");
        return gqs.findPermanentById(gd, id);
    }

    private List<Card> createCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new DefiantKhenra());
        }
        return cards;
    }
}
