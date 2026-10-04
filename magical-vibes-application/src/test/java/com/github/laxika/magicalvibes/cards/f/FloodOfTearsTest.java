package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FloodOfTears.class, GrizzlyBears.class, Island.class, SerraAngel.class, RaiseTheAlarm.class})
class FloodOfTearsTest extends BaseCardTest {

    @Test
    @DisplayName("Returns all nonland permanents and leaves lands on the battlefield")
    void returnsNonlandPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new FloodOfTears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Serra Angel");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Offers a permanent from hand after returning four controlled nontoken permanents")
    void offersPermanentAfterReturningFourControlledNontokens() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FloodOfTears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not offer the permanent rider below four returned controlled nontokens")
    void doesNotOfferPermanentBelowThreshold() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FloodOfTears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Serra Angel", "Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("The caster may decline to put a permanent onto the battlefield")
    void mayDeclinePermanent() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new FloodOfTears(), new SerraAngel()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);
        harness.assertInHand(player1, "Serra Angel");
        harness.assertInGraveyard(player1, "Flood of Tears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A returned permanent can be put back without paying its mana cost")
    void mayPutReturnedPermanentBack() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new FloodOfTears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Flood of Tears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The permanent put onto the battlefield may be a land")
    void mayPutLandOntoBattlefield() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.setHand(player1, List.of(new FloodOfTears(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotInHand(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponents' permanents do not contribute to the caster's threshold")
    void opponentsPermanentsDoNotCount() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new FloodOfTears()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player2, "Serra Angel");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controlled permanents owned by an opponent count but return to their owner")
    void controlledOpponentsPermanentCounts() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        SerraAngel stolenAngel = new SerraAngel();
        stolenAngel.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, stolenAngel);
        harness.setHand(player1, List.of(new FloodOfTears(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInHand(player2, "Serra Angel");
        harness.assertNotInHand(player1, "Serra Angel");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Tokens are returned but do not contribute to the nontoken threshold")
    void tokensDoNotCount() {
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new FloodOfTears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(2);
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player1, new GrizzlyBears());
        }
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears", "Grizzly Bears", "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
