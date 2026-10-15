package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Geistflame.class, WalkingCorpse.class, VampireInterloper.class, LilianaOfTheVeil.class, Mountain.class})
class GeistflameTest extends BaseCardTest {

    @Test
    @DisplayName("Geistflame deals 1 damage to target player")
    void deals1DamageToPlayer() {
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Geistflame deals 1 damage to target creature")
    void deals1DamageToCreature() {
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        UUID creatureId = permanent.getId();
        harness.castInstant(player1, 0, creatureId);
        harness.passBothPriorities();

        assertThat(permanent.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Geistflame goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Geistflame");
    }

    @Test
    @DisplayName("Flashback from graveyard deals 1 damage to target player")
    void flashbackDeals1DamageToPlayer() {
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Geistflame");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geistflame"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsInstant() {
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Geistflame");
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new Geistflame()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback kills a one-toughness creature and exiles Geistflame")
    void flashbackKillsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VampireInterloper());
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vampire Interloper");
        harness.assertInGraveyard(player2, "Vampire Interloper");
        harness.assertNotInGraveyard(player1, "Geistflame");
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geistflame"));
    }

    @Test
    @DisplayName("Flashback is exiled even when its target dies in response")
    void flashbackExilesWhenTargetBecomesIllegal() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new VampireInterloper());
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, creature.getId());
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Vampire Interloper");
        harness.passBothPriorities();

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().getPlayerExiledCards(player1.getId()))
                .filteredOn(c -> c.getName().equals("Geistflame")).hasSize(1);
        assertThat(harness.getGameData().playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Geistflame")).hasSize(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Geistflame can damage a planeswalker directly")
    void damagesPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new LilianaOfTheVeil());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Liliana of the Veil");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An ordinary land is not a legal target for Geistflame")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInHand(player1, "Geistflame");
    }

    @Test
    @DisplayName("Flashback requires red mana in addition to three generic mana")
    void flashbackRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new Geistflame()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(harness.getGameData().stack).isEmpty();
        harness.assertInGraveyard(player1, "Geistflame");
    }
}
