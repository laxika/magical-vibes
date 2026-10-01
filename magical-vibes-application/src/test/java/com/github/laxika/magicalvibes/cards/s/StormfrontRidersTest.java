package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.d.DuneriderOutlaw;
import com.github.laxika.magicalvibes.cards.f.FrozenAether;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormfrontRiders.class, DuneriderOutlaw.class, FrozenAether.class, Boomerang.class})
class StormfrontRidersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returning two creatures creates two Soldier tokens")
    void etbReturningTwoCreaturesCreatesTwoSoldiers() {
        UUID firstCreatureId = harness.addToBattlefieldAndReturn(player1, new DuneriderOutlaw()).getId();
        UUID secondCreatureId = harness.addToBattlefieldAndReturn(player1, new DuneriderOutlaw()).getId();
        harness.setHand(player1, List.of(new StormfrontRiders()));
        addStormfrontMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstCreatureId, secondCreatureId));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Dunerider Outlaw")))
                .hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
        assertThat(countPermanents(player1, "Stormfront Riders")).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB can return Stormfront Riders itself")
    void etbCanReturnItself() {
        harness.addToBattlefield(player1, new DuneriderOutlaw());
        harness.setHand(player1, List.of(new StormfrontRiders()));
        addStormfrontMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Stormfront Riders");
        harness.assertInHand(player1, "Stormfront Riders");
        assertThat(findPermanents(player1, "Soldier")).hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("ETB returns only creatures controlled by Stormfront Riders' controller")
    void etbReturnsOnlyControlledCreatures() {
        harness.addToBattlefield(player1, new DuneriderOutlaw());
        harness.addToBattlefield(player2, new DuneriderOutlaw());
        harness.setHand(player1, List.of(new StormfrontRiders()));
        addStormfrontMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Dunerider Outlaw");
        harness.assertNotOnBattlefield(player1, "Stormfront Riders");
        assertThat(findPermanents(player1, "Soldier")).hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Returning Stormfront Riders itself also creates a Soldier token")
    void returningSelfAlsoCreatesSoldier() {
        harness.addToBattlefield(player1, new DuneriderOutlaw());
        harness.addToBattlefield(player1, new StormfrontRiders());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID stormfrontId = harness.getPermanentId(player1, "Stormfront Riders");
        harness.castInstant(player1, 0, stormfrontId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(countPermanents(player1, "Stormfront Riders")).isZero();
    }

    @Test
    @DisplayName("Returning a noncreature does not trigger Stormfront Riders")
    void returningNoncreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new StormfrontRiders());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new FrozenAether());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, noncreature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Frozen Aether");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void addStormfrontMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
