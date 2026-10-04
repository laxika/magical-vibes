package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.s.SorinLordOfInnistrad;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiresOfUndeath.class, DawntreaderElk.class, SorinLordOfInnistrad.class})
class FiresOfUndeathTest extends BaseCardTest {

    

    @Test
    @DisplayName("Fires of Undeath deals 2 damage to target player")
    void deals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Fires of Undeath deals 2 damage to target creature, destroying a 2/2")
    void deals2DamageToCreatureDestroysIt() {
        harness.addToBattlefield(player2, new DawntreaderElk());
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Dawntreader Elk");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertInGraveyard(player2, "Dawntreader Elk");
    }

    @Test
    @DisplayName("Cannot cast Fires of Undeath without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Fires of Undeath goes to graveyard after normal cast")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Fires of Undeath");
    }

    @Test
    @DisplayName("Flashback from graveyard deals 2 damage to target player")
    void flashbackDeals2DamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Flashback spell is exiled after resolving, not sent to graveyard")
    void flashbackExilesAfterResolving() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotInGraveyard(player1, "Fires of Undeath");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Fires of Undeath"));
    }

    @Test
    @DisplayName("Flashback puts spell on stack as instant spell")
    void flashbackPutsOnStackAsSpell() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Fires of Undeath");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
        assertThat(entry.isCastWithFlashback()).isTrue();
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Damage removes two loyalty counters from a planeswalker")
    void damagesPlaneswalker() {
        var sorin = harness.addToBattlefieldAndReturn(player2, new SorinLordOfInnistrad());
        sorin.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, sorin.getId());
        harness.passBothPriorities();

        assertThat(sorin.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Sorin, Lord of Innistrad");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Normal casting requires red mana")
    void normalCastRequiresRedMana() {
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Fires of Undeath");
    }

    @Test
    @DisplayName("Flashback requires black mana rather than red mana")
    void flashbackRequiresBlackMana() {
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Fires of Undeath");
    }

    @Test
    @DisplayName("Flashback deals lethal damage to a creature and exiles the spell")
    void flashbackKillsCreature() {
        var elk = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        harness.setGraveyard(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castFlashback(player1, 0, elk.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.assertInGraveyard(player2, "Dawntreader Elk");
        harness.assertNotInGraveyard(player1, "Fires of Undeath");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Fires of Undeath"));
    }

    @Test
    @DisplayName("Flashback is exiled even when its sole target becomes illegal")
    void flashbackExilesWithIllegalTarget() {
        var elk = harness.addToBattlefieldAndReturn(player2, new DawntreaderElk());
        var flashbackCard = new FiresOfUndeath();
        harness.setGraveyard(player1, List.of(flashbackCard));
        harness.setHand(player1, List.of(new FiresOfUndeath()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0, elk.getId());
        harness.castInstant(player1, 0, elk.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Dawntreader Elk");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(flashbackCard);
        harness.assertInGraveyard(player1, "Fires of Undeath");
    }
}
