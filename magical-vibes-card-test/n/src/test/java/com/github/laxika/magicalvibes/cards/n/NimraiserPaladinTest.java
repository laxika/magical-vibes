package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AnnihilatingGlare;
import com.github.laxika.magicalvibes.cards.s.StingingHivemaster;
import com.github.laxika.magicalvibes.cards.t.ThunderingGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimraiserPaladin.class, GrizzlyBears.class, ThunderingGiant.class,
        StingingHivemaster.class, AnnihilatingGlare.class})
class NimraiserPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Toxic 2 gives the defending player two poison counters on combat damage")
    void toxicDealsTwoPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent paladin = addCreatureReady(player1, new NimraiserPaladin());
        paladin.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
    }

    @Test
    @DisplayName("The ETB returns a targeted creature with mana value 3 or less to hand")
    void etbReturnsEligibleCreatureToHand() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        castAndResolvePaladin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The ETB cannot target a creature with mana value greater than 3")
    void etbCannotTargetHighManaValueCreature() {
        harness.setGraveyard(player1, List.of(new ThunderingGiant()));
        castAndResolvePaladin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Thundering Giant");
    }

    @Test
    @DisplayName("Toxic gives poison counters during combat damage without using the stack")
    void toxicAppliesAlongsideCombatDamage() {
        Permanent paladin = addCreatureReady(player1, new NimraiserPaladin());
        paladin.setAttacking(true);
        harness.forceActivePlayer(player1);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 16);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A creature with mana value exactly three is an eligible target")
    void etbReturnsCreatureAtManaValueBoundary() {
        StingingHivemaster target = new StingingHivemaster();
        harness.setGraveyard(player1, List.of(target));
        castAndResolvePaladin();

        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Stinging Hivemaster");
        harness.assertNotInGraveyard(player1, "Stinging Hivemaster");
        harness.assertNotOnBattlefield(player1, "Stinging Hivemaster");
    }

    @Test
    @DisplayName("The ETB cannot return noncreatures or creatures from an opponent's graveyard")
    void etbHasNoTargetsWithOnlyWrongTypeOrWrongGraveyard() {
        harness.setGraveyard(player1, List.of(new AnnihilatingGlare()));
        harness.setGraveyard(player2, List.of(new StingingHivemaster()));
        castAndResolvePaladin();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Annihilating Glare");
        harness.assertInGraveyard(player2, "Stinging Hivemaster");
    }

    @Test
    @DisplayName("The ETB does not return a target that leaves the graveyard before resolution")
    void etbDoesNotReturnMissingTarget() {
        StingingHivemaster target = new StingingHivemaster();
        harness.setGraveyard(player1, List.of(target));
        castAndResolvePaladin();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Stinging Hivemaster");
        assertThat(gd.findExiledCard(target.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolvePaladin() {
        harness.setHand(player1, List.of(new NimraiserPaladin()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
