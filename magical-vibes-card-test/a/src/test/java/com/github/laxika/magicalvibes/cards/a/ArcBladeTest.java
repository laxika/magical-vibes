package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.c.Clockspinning;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcBlade.class, BlindPhantasm.class, Clockspinning.class})
class ArcBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to a target player and is exiled with three time counters")
    void dealsDamageAndIsExiledWithSuspendCounters() {
        ArcBlade blade = new ArcBlade();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(blade));
        addCastMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(blade.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Deals 2 damage to a target creature")
    void dealsDamageToTargetCreature() {
        Permanent creature = addCreatureReady(player2, new BlindPhantasm());
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        addCastMana();

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
    }

    @Test
    @DisplayName("Suspend exiles Arc Blade with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        ArcBlade blade = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.exiledCardTimeCounters).containsEntry(blade.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A suspended Arc Blade deals damage when cast for free")
    void suspendedCardCastsForFree() {
        harness.setLife(player2, 20);
        ArcBlade blade = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(blade.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Declining the free suspend cast leaves Arc Blade exiled without time counters")
    void decliningSuspendCastLeavesCardExiled() {
        ArcBlade blade = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(blade);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(blade.getId());
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    @DisplayName("Clockspinning can remove a time counter from Arc Blade after it resolves")
    void canRemoveTimeCounterAfterResolution() {
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        addCastMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.setHand(player1, List.of(new Clockspinning()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, blade.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "time counters");
        harness.handleListChoice(player1, "REMOVE");

        for (int i = 0; i < 2; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
    }

    @Test
    @DisplayName("Arc Blade can be cast again three upkeeps after resolving")
    void repeatsAfterResolving() {
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        addCastMana();
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(blade);
        assertThat(gd.suspendedSpellExiles)
                .containsExactly(new GameData.SuspendedSpellExile(blade.getId(), player1.getId(), 3));
    }

    @Test
    @DisplayName("Arc Blade is put into the graveyard if its only target becomes illegal")
    void illegalTargetPreventsSelfExile() {
        Permanent creature = addCreatureReady(player2, new BlindPhantasm());
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        addCastMana();
        harness.castSorcery(player1, 0, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blade);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(blade);
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private ArcBlade suspendCard() {
        ArcBlade blade = new ArcBlade();
        harness.setHand(player1, List.of(blade));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateHandAbility(player1, 0, null);
        return blade;
    }
}
