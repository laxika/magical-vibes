package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomBertAndWilliam.class, Disenchant.class, Forest.class, GoForTheThroat.class,
        GrizzlyBears.class, Shock.class, SoulWarden.class, Swamp.class})
class TomBertAndWilliamTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature draws cards equal to its effective power, then discards")
    void sacrificeAbilityDrawsAndDiscards() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        sacrificed.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Swamp(), new Shock(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tom);
    }

    @Test
    @DisplayName("When it dies as a creature, it returns as a noncreature artifact")
    void returnsAsNoncreatureArtifact() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        destroyCreature(tom);

        Permanent returned = findPermanent(player1, "Tom, Bert, and William");
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        harness.assertNotInGraveyard(player1, "Tom, Bert, and William");
    }

    @Test
    @DisplayName("A returned artifact does not trigger the creature death ability again")
    void returnedArtifactDoesNotReturnAgain() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        destroyCreature(tom);
        Permanent returned = findPermanent(player1, "Tom, Bert, and William");

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, returned.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tom, Bert, and William");
        harness.assertNotOnBattlefield(player1, "Tom, Bert, and William");
    }

    @Test
    @DisplayName("A creature controlled by a player other than its owner still returns")
    void returnsFromOwnersGraveyardUnderLastControllersControl() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        gd.stolenCreatures.put(tom.getId(), player2.getId());

        destroyCreature(tom);

        harness.assertNotInGraveyard(player2, "Tom, Bert, and William");
        harness.assertNotOnBattlefield(player2, "Tom, Bert, and William");
        Permanent returned = findPermanent(player1, "Tom, Bert, and William");
        assertThat(gqs.isArtifact(gd, returned)).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
    }

    @Test
    @DisplayName("The returned artifact retains its sacrifice ability")
    void returnedArtifactCanDrawAndDiscard() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        destroyCreature(tom);
        Permanent returned = findPermanent(player1, "Tom, Bert, and William");
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Swamp(), new Forest(), new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
    }

    @Test
    @DisplayName("Sacrificing a zero-power creature draws nothing but still discards")
    void zeroPowerStillDiscards() {
        addCreatureReady(player1, new TomBertAndWilliam());
        Permanent sacrificed = addCreatureReady(player1, new GrizzlyBears());
        sacrificed.setPowerModifier(-2);
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Swamp()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Tom, Bert, and William cannot sacrifice themselves to their ability")
    void cannotActivateWithoutAnotherCreature() {
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tom);
        harness.assertNotInGraveyard(player1, "Tom, Bert, and William");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning as an artifact does not trigger Soul Warden")
    void returnsWithoutCreatureEntryTriggers() {
        addCreatureReady(player2, new SoulWarden());
        Permanent tom = addCreatureReady(player1, new TomBertAndWilliam());
        int lifeBeforeReturn = gd.playerLifeTotals.get(player2.getId());

        destroyCreature(tom);

        harness.assertOnBattlefield(player1, "Tom, Bert, and William");
        harness.assertLife(player2, lifeBeforeReturn);
    }
    private void destroyCreature(Permanent permanent) {
        harness.setHand(player1, List.of(new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, permanent.getId());
        resolveAllTriggers();
    }
}
