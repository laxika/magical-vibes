package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaOfTheVeil;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HereticsPunishment.class, Forest.class, GrizzlyBears.class, Shock.class, HillGiant.class})
class HereticsPunishmentTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability targeting a player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        addHereticsPunishment(player1);
        addActivationMana(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard()).isInstanceOf(HereticsPunishment.class);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumed() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills three cards and deals damage equal to highest mana value to target player")
    void millsAndDealsDamageToPlayer() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // Set up library with known cards: Shock (MV 1), GrizzlyBears (MV 2), HillGiant (MV 4)
        harness.setLibrary(player1, List.of(
                new Shock(), new GrizzlyBears(), new HillGiant()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Highest mana value is 4 (HillGiant), so 4 damage to player2
        harness.assertLife(player2, 16);
        // Controller's library should be empty (3 cards milled)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        // All 3 cards should be in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest mana value, not the sum")
    void dealsGreatestManaValueNotSum() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // All cards have MV 2 → damage should be 2, not 6
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals damage to target creature equal to highest mana value among milled cards")
    void dealsDamageToCreature() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Set up library: HillGiant (MV 4) is highest → 4 damage kills 2/2
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new HillGiant()
        ));

        harness.activateAbility(player1, 0, null, bearsId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Deals 0 damage when all milled cards have mana value 0")
    void dealsZeroDamageWhenAllLands() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // All lands have MV 0
        harness.setLibrary(player1, List.of(
                new Forest(), new Forest(), new Forest()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mills fewer than three cards when library has fewer than three cards")
    void millsFewerWhenLibrarySmall() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // Only 1 card in library: GrizzlyBears (MV 2)
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Deals no damage when library is empty")
    void dealsNoDamageWhenLibraryEmpty() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can be activated multiple times per turn (no tap cost)")
    void canActivateMultipleTimes() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);

        // 6 cards in library for two activations
        harness.setLibrary(player1, List.of(
                new Shock(), new GrizzlyBears(), new HillGiant(),
                new Shock(), new GrizzlyBears(), new HillGiant()
        ));

        // First activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Second activation
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        // Both activations deal 4 damage (HillGiant MV 4 is highest in each set)
        harness.assertLife(player2, 12);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({BruvacTheGrandiloquent.class})
    @DisplayName("Damage includes all cards milled when an opponent doubles the mill")
    void includesCardsAddedByMillReplacement() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addToBattlefield(player2, new BruvacTheGrandiloquent());
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(),
                new HillGiant(), new Forest(), new Forest()
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An illegal target prevents milling as well as damage")
    void doesNotMillWhenTargetLeavesBattlefield() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        HillGiant libraryCard = new HillGiant();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability resolves after its source leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest(), new Shock()));

        harness.activateAbility(player1, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Only the top three cards contribute to damage without a replacement")
    void ignoresUnmilledCards() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.setLife(player2, 20);
        HillGiant remainingCard = new HillGiant();
        harness.setLibrary(player1, List.of(
                new Forest(), new Shock(), new GrizzlyBears(), remainingCard
        ));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @CardUsed({LilianaOfTheVeil.class})
    @DisplayName("Can target and destroy a planeswalker through damage")
    void dealsDamageToPlaneswalker() {
        addHereticsPunishment(player1);
        addActivationMana(player1);
        harness.addToBattlefield(player2, new LilianaOfTheVeil());
        harness.setLibrary(player1, List.of(new HillGiant(), new Forest(), new Shock()));

        harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player2, "Liliana of the Veil"));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana of the Veil");
        harness.assertInGraveyard(player2, "Liliana of the Veil");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    private void addHereticsPunishment(Player player) {
        harness.addToBattlefield(player, new HereticsPunishment());
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 3);
    }
}
