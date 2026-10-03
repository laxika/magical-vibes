package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CastDown;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.Frogify;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hushbringer;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.t.TendThePests;
import com.github.laxika.magicalvibes.cards.t.TeysaKarlov;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DaemogothWoeEater.class, Forest.class, GrizzlyBears.class, CastDown.class,
        TendThePests.class, LeylineOfTheVoid.class, Hushbringer.class, Frogify.class, TeysaKarlov.class})
class DaemogothWoeEaterTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of your upkeep, it sacrifices itself and its sacrifice trigger resolves")
    void upkeepSacrificeTriggersDiscardDrawAndLifeGain() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(daemogoth.getId()));
        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player1, 12);
    }

    @Test
    @DisplayName("Destroying it without sacrificing it does not trigger its sacrifice ability")
    void destructionDoesNotTriggerSacrificeAbility() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, new ArrayList<>(List.of(new CastDown())));
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLife(player1, 10);

        harness.castAndResolveInstant(player1, 0, daemogoth.getId());

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSacrificeAnotherCreatureWithoutReceivingTheReward() {
        harness.addToBattlefield(player1, new DaemogothWoeEater());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, other.getId());

        harness.assertOnBattlefield(player1, "Daemogoth Woe-Eater");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsUpkeepDoesNotRequireASacrifice() {
        harness.addToBattlefield(player1, new DaemogothWoeEater());

        advanceToUpkeep(player2);

        harness.assertOnBattlefield(player1, "Daemogoth Woe-Eater");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emptyOpponentHandDoesNotPreventDrawOrLifeGain() {
        harness.addToBattlefield(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 12);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void sacrificeAsAnAdditionalCostTriggersBeforeTheSpellResolves() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new TendThePests()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLife(player1, 10);

        harness.castInstantWithSacrifice(player1, 0, null, daemogoth.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(countPermanents(player1, "Pest")).isZero();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Pest")).isEqualTo(7);
        harness.assertLife(player1, 12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeRewardTriggersEvenWhenTheSacrificedCardIsExiled() {
        harness.addToBattlefield(player1, new DaemogothWoeEater());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        harness.assertNotOnBattlefield(player1, "Daemogoth Woe-Eater");
        harness.assertNotInGraveyard(player1, "Daemogoth Woe-Eater");
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getName().equals("Daemogoth Woe-Eater"));
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hushbringerDoesNotSuppressTheSacrificeReward() {
        harness.addToBattlefield(player1, new DaemogothWoeEater());
        harness.addToBattlefield(player2, new Hushbringer());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        harness.assertInHand(player1, "Forest");
        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void losingAbilitiesBeforeSacrificePreventsTheReward() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.setHand(player1, List.of(new Frogify()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, daemogoth.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new TendThePests()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstantWithSacrifice(player1, 0, null, daemogoth.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        harness.assertLife(player1, 10);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(countPermanents(player1, "Pest")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void teysaKarlovDoesNotMultiplyTheSacrificeReward() {
        Permanent daemogoth = harness.addToBattlefieldAndReturn(player1, new DaemogothWoeEater());
        harness.addToBattlefield(player1, new TeysaKarlov());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLife(player1, 10);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, daemogoth.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Daemogoth Woe-Eater");
        harness.assertLife(player1, 12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
