package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AwakenerDruid;
import com.github.laxika.magicalvibes.cards.c.CoastalDiscovery;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HalimarTidecaller.class, CoastalDiscovery.class, GrizzlyBears.class, Forest.class,
        AwakenerDruid.class})
class HalimarTidecallerTest extends BaseCardTest {

    @Test
    void returnsTargetAwakenCardFromGraveyardToHand() {
        Card awakenCard = new CoastalDiscovery();
        Card nonAwakenCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(awakenCard, nonAwakenCard));
        castTidecaller();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(awakenCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(awakenCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Coastal Discovery");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void decliningReturnLeavesAwakenCardInGraveyard() {
        Card awakenCard = new CoastalDiscovery();
        harness.setGraveyard(player1, List.of(awakenCard));
        castTidecaller();

        harness.handleMultipleCardsChosen(player1, List.of(awakenCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Coastal Discovery");
        harness.assertNotInHand(player1, "Coastal Discovery");
    }

    @Test
    void landCreaturesYouControlHaveFlying() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new HalimarTidecaller());

        assertThat(gqs.hasKeyword(gd, ownLand, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLand, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, ownLand.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, ownLand, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentLand, com.github.laxika.magicalvibes.model.Keyword.FLYING)).isFalse();
    }

    private void castTidecaller() {
        harness.castFromHand(player1, new HalimarTidecaller(), "{2}{U}");
        harness.passBothPriorities();
    }

    @Test
    void entersWithoutPromptWhenNoAwakenCardIsInYourGraveyard() {
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new CoastalDiscovery()));

        castTidecaller();

        harness.assertOnBattlefield(player1, "Halimar Tidecaller");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Coastal Discovery");
    }

    @Test
    void opponentAwakenCardIsNotOfferedAsATarget() {
        Card ownCard = new CoastalDiscovery();
        Card opponentCard = new CoastalDiscovery();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        castTidecaller();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Coastal Discovery");
        harness.assertInGraveyard(player2, "Coastal Discovery");
    }

    @Test
    void returnDoesNothingWhenTargetLeavesGraveyardBeforeResolution() {
        Card awakenCard = new CoastalDiscovery();
        harness.setGraveyard(player1, List.of(awakenCard));
        castTidecaller();
        harness.handleMultipleCardsChosen(player1, List.of(awakenCard.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(awakenCard));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Coastal Discovery");
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isSameAs(awakenCard));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void awakenedLandLosesFlyingWhenTidecallerLeavesBattlefield() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent tidecaller = harness.addToBattlefieldAndReturn(player1, new HalimarTidecaller());
        harness.setHand(player1, List.of(new CoastalDiscovery()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithAlternateCost(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, tidecaller, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(tidecaller);
        harness.setGraveyard(player1, List.of(tidecaller.getCard()));

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
    }

    @Test
    void animatedOpponentLandDoesNotHaveFlying() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new HalimarTidecaller());
        harness.setHand(player1, List.of(new AwakenerDruid()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, land.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.FLYING)).isFalse();
    }
}
