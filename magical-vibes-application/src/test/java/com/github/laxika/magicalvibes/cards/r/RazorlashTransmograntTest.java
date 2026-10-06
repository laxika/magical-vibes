package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.g.GarruksUprising;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorlashTransmogrant.class, Island.class, EvolvingWilds.class,
        GarruksUprising.class, Disfigure.class})
class RazorlashTransmograntTest extends BaseCardTest {

    @Test
    @DisplayName("Can't block")
    void cantBlock() {
        Permanent transmogrant = harness.addToBattlefieldAndReturn(player1, new RazorlashTransmogrant());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new RazorlashTransmogrant());

        assertThat(bls.canBlockAttacker(gd, transmogrant, blocker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Returns from the graveyard with a +1/+1 counter for the full cost")
    void returnsForFullCost() {
        RazorlashTransmogrant transmogrant = new RazorlashTransmogrant();
        harness.setGraveyard(player1, List.of(transmogrant));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Razorlash Transmogrant");
        assertThat(returned.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Costs only the two black mana when an opponent controls four nonbasic lands")
    void costsLessWithFourOpponentNonbasicLands() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new EvolvingWilds());
        }
        RazorlashTransmogrant transmogrant = new RazorlashTransmogrant();
        harness.setGraveyard(player1, List.of(transmogrant));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Razorlash Transmogrant")
                .getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("Basic lands do not satisfy the cost reduction condition")
    void basicLandsDoNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Island());
        }
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void threeNonbasicLandsAndABasicLandDoNotReduceCost() {
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new EvolvingWilds());
        }
        harness.addToBattlefield(player2, new Island());
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void ownNonbasicLandsDoNotReduceCost() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new EvolvingWilds());
        }
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void returnsOnlyTheActivatedCopy() {
        RazorlashTransmogrant source = new RazorlashTransmogrant();
        RazorlashTransmogrant other = new RazorlashTransmogrant();
        harness.setGraveyard(player1, List.of(source, other));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Razorlash Transmogrant").getCard().getId()).isEqualTo(source.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    void entersWithCounterBeforePowerBasedEntryTriggersAreChecked() {
        harness.addToBattlefield(player1, new GarruksUprising());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Island");
    }

    @Test
    void olderActivationCannotReturnCardAfterItReturnsAndDiesAgain() {
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.setHand(player1, List.of(new Disfigure()));
        harness.addMana(player1, ManaColor.BLACK, 13);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Razorlash Transmogrant");
        harness.castInstant(player1, 0, returned.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Razorlash Transmogrant");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Razorlash Transmogrant");
        harness.assertInGraveyard(player1, "Razorlash Transmogrant");
    }

    @Test
    void canReturnDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.ensurePriority(player1);
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Razorlash Transmogrant");
        harness.assertNotInGraveyard(player1, "Razorlash Transmogrant");
    }

    @Test
    void discountDoesNotRemoveBlackManaRequirements() {
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new EvolvingWilds());
        }
        harness.setGraveyard(player1, List.of(new RazorlashTransmogrant()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
