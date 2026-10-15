package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineCongregation.class, AshcoatBear.class, Plains.class})
class DivineCongregationTest extends BaseCardTest {

    private void castDivineCongregation(Player targetPlayer) {
        harness.setHand(player1, List.of(new DivineCongregation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, targetPlayer.getId());
    }

    @Test
    @DisplayName("Gains 2 life for each creature the target opponent controls")
    void gainsTwoLifePerCreature() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.addToBattlefield(player2, new AshcoatBear());

        castDivineCongregation(player2);

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Only the target opponent's creatures count")
    void ignoresCastersCreatures() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AshcoatBear());
        harness.addToBattlefield(player2, new AshcoatBear());

        castDivineCongregation(player2);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counts creatures but not other permanents the target player controls")
    void ignoresNoncreaturePermanents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.addToBattlefield(player2, new Plains());

        castDivineCongregation(player2);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Gains no life when the opponent controls no creatures")
    void gainsNothingWithoutCreatures() {
        harness.setLife(player1, 20);

        castDivineCongregation(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Can target yourself")
    void canTargetSelf() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new AshcoatBear());

        castDivineCongregation(player1);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Counts creatures at resolution, including creatures that entered in response")
    void countsCreaturesAtResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new DivineCongregation()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, player2.getId());

        harness.addToBattlefield(player2, new AshcoatBear());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Divine Congregation");
    }

    @Test
    @DisplayName("Declining the suspended cast leaves the card exiled with no further upkeep trigger")
    void canDeclineSuspendedCast() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AshcoatBear());
        DivineCongregation card = new DivineCongregation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Suspend exiles Divine Congregation with five time counters and later casts it for free")
    void suspendCastsForFree() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new AshcoatBear());
        harness.addToBattlefield(player2, new AshcoatBear());

        DivineCongregation card = new DivineCongregation();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        harness.assertInGraveyard(player1, "Divine Congregation");
    }
}
