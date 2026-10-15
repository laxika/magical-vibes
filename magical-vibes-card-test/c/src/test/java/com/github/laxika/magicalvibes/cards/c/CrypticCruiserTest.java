package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EldraziDevastator;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrypticCruiser.class, EldraziDevastator.class, ScourFromExistence.class})
class CrypticCruiserTest extends BaseCardTest {

    @Test
    void putsOpponentOwnedExiledCardIntoItsOwnersGraveyardAndTapsTargetCreature() {
        Permanent cruiser = harness.addToBattlefieldAndReturn(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        ScourFromExistence exiledCard = new ScourFromExistence();
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(target.isTapped()).isTrue();
        assertThat(cruiser.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotAllowActivatingWithoutOpponentOwnedExiledCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.addToBattlefield(player1, new CrypticCruiser());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void cannotUseControllerOwnedExiledCardsAsCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        harness.addToBattlefield(player1, new CrypticCruiser());
        harness.setExile(player1, List.of(new ScourFromExistence()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void canChooseAmongMultipleOpponentOwnedExiledCards() {
        harness.addToBattlefield(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        ScourFromExistence first = new ScourFromExistence();
        ScourFromExistence second = new ScourFromExistence();
        harness.setExile(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void paysExileCostImmediatelyButTapsOnlyOnResolution() {
        harness.addToBattlefield(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        ScourFromExistence costCard = new ScourFromExistence();
        harness.setExile(player2, List.of(costCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertInGraveyard(player2, "Scour from Existence");
        assertThat(gd.findExiledCard(costCard.getId())).isNull();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void canTargetItselfAndActivateAgainWhileTappedAndSummoningSick() {
        Permanent cruiser = harness.addToBattlefieldAndReturn(player1, new CrypticCruiser());
        cruiser.setSummoningSick(true);
        ScourFromExistence first = new ScourFromExistence();
        ScourFromExistence second = new ScourFromExistence();
        harness.setExile(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, cruiser.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();
        assertThat(cruiser.isTapped()).isTrue();

        harness.activateAbility(player1, 0, null, cruiser.getId());
        harness.passBothPriorities();

        assertThat(cruiser.isTapped()).isTrue();
        assertThat(gd.findExiledCard(first.getId())).isNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotRefundCostWhenTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        ScourFromExistence costCard = new ScourFromExistence();
        harness.setExile(player2, List.of(costCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Eldrazi Devastator");
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(costCard.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(costCard);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayBlueManaRequirementWithOnlyColorlessMana() {
        harness.addToBattlefield(player1, new CrypticCruiser());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EldraziDevastator());
        ScourFromExistence costCard = new ScourFromExistence();
        harness.setExile(player2, List.of(costCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.findExiledCard(costCard.getId())).isNotNull();
        assertThat(target.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void choosingFaceDownExileCostDoesNotRevealHiddenCardIdentities() {
        Permanent cruiser = harness.addToBattlefieldAndReturn(player1, new CrypticCruiser());
        ScourFromExistence first = new ScourFromExistence();
        EldraziDevastator second = new EldraziDevastator();
        gd.addToExile(player2.getId(), first, cruiser.getId(), true);
        gd.addToExile(player2.getId(), second, cruiser.getId(), true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.clearMessages();

        harness.activateAbility(player1, 0, null, cruiser.getId());

        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"INTERACTION_PROMPT\""))
                .isNotEmpty()
                .allSatisfy(message -> assertThat(message)
                        .doesNotContain("Scour from Existence", "Eldrazi Devastator"));
    }
}
