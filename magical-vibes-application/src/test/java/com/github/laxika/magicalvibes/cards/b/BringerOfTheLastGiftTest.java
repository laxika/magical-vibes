package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BringerOfTheLastGift.class, BeaconOfUnrest.class, GrizzlyBears.class, SavannahLions.class})
class BringerOfTheLastGiftTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, sacrifices other creatures and returns pre-existing graveyard creatures")
    void castSacrificesOtherCreaturesAndReturnsPreExistingCreatures() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SavannahLions());
        harness.setGraveyard(player1, List.of(new SavannahLions()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bringer of the Last Gift")).isEqualTo(1);
        assertThat(countPermanents(player1, "Savannah Lions")).isEqualTo(1);
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Savannah Lions");
    }

    @Test
    @DisplayName("When cast, does not return creatures sacrificed by its ability")
    void doesNotReturnCreaturesSacrificedByItsAbility() {
        harness.setHand(player1, List.of(new BringerOfTheLastGift()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SavannahLions());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Savannah Lions");
        assertThat(countPermanents(player1, "Grizzly Bears")).isZero();
        assertThat(countPermanents(player2, "Savannah Lions")).isZero();
    }

    @Test
    @DisplayName("When put onto the battlefield without being cast, it does not trigger")
    void nonCastEtbDoesNotTrigger() {
        harness.setGraveyard(player1, List.of(new BringerOfTheLastGift()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bringer of the Last Gift");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player2, "Savannah Lions")).isZero();
    }
}
