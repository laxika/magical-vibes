package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.h.HiredGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CateranOverlord.class, CateranEnforcer.class, CateranSlaver.class,
        CeremonialGuard.class, HiredGiant.class})
class CateranOverlordTest extends BaseCardTest {

    @Test
    void sacrificingACreatureRegeneratesCateranOverlord() {
        Permanent overlord = addCreatureReady(player1, new CateranOverlord());
        Permanent fodder = addCreatureReady(player1, new CeremonialGuard());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ceremonial Guard");
        assertThat(overlord.getRegenerationShield()).isEqualTo(1);
        assertThat(overlord.isTapped()).isFalse();
    }

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostSix() {
        Permanent overlord = addCreatureReady(player1, new CateranOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.setLibrary(player1, List.of(new CateranEnforcer(), new CeremonialGuard(), new CateranOverlord()));

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(overlord.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Enforcer");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Enforcer");
        harness.assertNotOnBattlefield(player1, "Ceremonial Guard");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Ceremonial Guard", "Cateran Overlord");
    }

    @Test
    void searchesForMercenaryWithManaValueExactlySix() {
        addCreatureReady(player1, new CateranOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLibrary(player1, List.of(new CateranSlaver(), new CeremonialGuard()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Slaver");

        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Cateran Slaver");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Ceremonial Guard");
    }

    @Test
    void resolvesWithoutInteractionWhenNoEligibleMercenaryPermanentExists() {
        Permanent overlord = addCreatureReady(player1, new CateranOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.setLibrary(player1, List.of(new CateranOverlord(), new CeremonialGuard()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(overlord.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Cateran Overlord", "Ceremonial Guard");
    }

    @Test
    void regenerationShieldSavesCateranOverlordFromLethalCombatDamage() {
        Permanent overlord = addCreatureReady(player1, new CateranOverlord());
        overlord.setRegenerationShield(1);
        overlord.setAttacking(true);

        Permanent firstBlocker = addCreatureReady(player2, new HiredGiant());
        firstBlocker.setBlocking(true);
        firstBlocker.addBlockingTarget(0);
        Permanent secondBlocker = addCreatureReady(player2, new HiredGiant());
        secondBlocker.setBlocking(true);
        secondBlocker.addBlockingTarget(0);

        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                firstBlocker.getId(), 4,
                secondBlocker.getId(), 3));

        harness.assertOnBattlefield(player1, "Cateran Overlord");
        assertThat(overlord.getRegenerationShield()).isZero();
        assertThat(overlord.isTapped()).isTrue();
        assertThat(overlord.isAttacking()).isFalse();
        assertThat(overlord.getMarkedDamage()).isZero();
    }
}
