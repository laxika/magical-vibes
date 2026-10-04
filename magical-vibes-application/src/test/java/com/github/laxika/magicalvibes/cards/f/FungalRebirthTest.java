package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BloodthroneVampire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FungalRebirth.class, BloodthroneVampire.class, GrizzlyBears.class,
        HolyDay.class, LeoninScimitar.class, TormodsCrypt.class})
class FungalRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target permanent card from the graveyard to its owner's hand")
    void returnsTargetPermanentCardToHand() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Creates two Saproling tokens if a creature died this turn")
    void createsTwoSaprolingsWithMorbid() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new BloodthroneVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        java.util.UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bearsId);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a nonpermanent card in a graveyard")
    void cannotTargetNonpermanentCard() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("permanent card");
    }

    @Test
    void returnsCreatureCardWithoutTreatingItsPresenceInGraveyardAsADeath() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }

    @Test
    void cannotTargetOpponentsPermanentCard() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    void checksForCreatureDeathAtResolution() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new BloodthroneVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SAPROLING);
        });
    }

    @Test
    void countsAnOpponentsCreatureDeath() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player2, new BloodthroneVampire());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.handlePermanentChosen(player2, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Leonin Scimitar");
        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    void createsNoTokensWhenItsOnlyTargetLeavesTheGraveyard() {
        Card target = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(target));
        harness.addToBattlefield(player1, new BloodthroneVampire());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new TormodsCrypt());
        harness.setHand(player1, List.of(new FungalRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Fungal Rebirth");
        assertThat(findPermanents(player1, "Saproling")).isEmpty();
    }
}
