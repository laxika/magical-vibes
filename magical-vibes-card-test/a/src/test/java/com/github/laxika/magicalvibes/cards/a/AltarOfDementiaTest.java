package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltarOfDementia.class, Disenchant.class, EliteVanguard.class, GrizzlyBears.class, SerraAngel.class})
class AltarOfDementiaTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills cards equal to the sacrificed creature's power")
    void millsEqualToSacrificedPower() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4/4
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        harness.assertInGraveyard(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's effective (boosted) power")
    void usesEffectivePower() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power 3
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new EliteVanguard()); // 2/1
        trimDeck(player1, 10);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(3); // 2 milled cards plus the sacrificed Elite Vanguard
    }

    @Test
    @DisplayName("Mill is capped by library size")
    void millCappedByLibrarySize() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4 power
        trimDeck(player2, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Controller chooses which creature to sacrifice")
    void choosesAmongCreatures() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new SerraAngel()); // 4/4
        addCreatureReady(player1, new EliteVanguard()); // 2/1
        UUID vanguard = harness.getPermanentId(player1, "Elite Vanguard");
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, vanguard);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Elite Vanguard");
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("Cannot activate without a creature to sacrifice")
    void cannotActivateWithoutCreature() {
        harness.addToBattlefield(player1, new AltarOfDementia());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new EliteVanguard());
        UUID altarId = harness.getPermanentId(player1, "Altar of Dementia");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, altarId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature can still be sacrificed")
    void summoningSickCreatureCanBeSacrificed() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        harness.addToBattlefield(player1, new SerraAngel());
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Sacrifice is paid before the mill ability resolves")
    void sacrificeIsPaidImmediately() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player1, new GrizzlyBears());
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(8);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Sacrificing a zero-power creature mills no cards")
    void zeroPowerMillsNothing() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-2);
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a negative-power creature mills no cards")
    void negativePowerMillsNothing() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(-3);
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(10);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        addCreatureReady(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Queued activations retain their own sacrificed power and target")
    void queuedActivationsKeepSeparatePowerAndTargets() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent boostedBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        boostedBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        trimDeck(player1, 10);
        trimDeck(player2, 10);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, boostedBears.getId());
        harness.activateAbility(player1, 0, null, player1.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Targeting an empty library is legal and does not cause a loss")
    void canMillAnEmptyLibrary() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The mill ability resolves after Altar of Dementia is destroyed")
    void resolvesAfterAltarIsDestroyed() {
        harness.addToBattlefield(player1, new AltarOfDementia());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        bears.tap();
        trimDeck(player2, 10);
        UUID altarId = harness.getPermanentId(player1, "Altar of Dementia");
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.castAndResolveInstant(player2, 0, altarId);

        harness.assertNotOnBattlefield(player1, "Altar of Dementia");
        harness.assertInGraveyard(player1, "Altar of Dementia");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> !(card instanceof Disenchant)).hasSize(3);
        assertThat(gd.stack).isEmpty();
    }

    private void trimDeck(com.github.laxika.magicalvibes.model.Player player, int size) {
        List<Card> deck = gd.playerDecks.get(player.getId());
        harness.setLibrary(player, deck.subList(Math.max(0, deck.size() - size), deck.size()));
    }
}
