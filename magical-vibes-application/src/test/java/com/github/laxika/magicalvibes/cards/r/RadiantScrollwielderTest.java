package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.p.PigmentStorm;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadiantScrollwielder.class, GrizzlyBears.class, Shock.class, Humble.class, PigmentStorm.class})
class RadiantScrollwielderTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells you control have lifelink")
    void controllerInstantHasLifelink() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Upkeep exiles a random instant or sorcery from your graveyard and grants cast permission")
    void upkeepExilesMatchingGraveyardCard() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), shock));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(shock.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(shock.getId());
        assertThat(gd.exileInsteadOfGraveyard).contains(shock.getId());
    }

    @Test
    @DisplayName("A spell cast from the exiled card is exiled instead of entering a graveyard")
    void castSpellFromExileRemainsExiled() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, shock.getId(), bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Upkeep ability does nothing when your graveyard has no instant or sorcery")
    void upkeepWithNoMatchingCardDoesNothing() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bears);
    }

    @Test
    void losingAbilitiesStopsGrantingSpellLifelink() {
        Permanent scrollwielder = harness.addToBattlefieldAndReturn(player1, new RadiantScrollwielder());
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, scrollwielder.getId());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void opponentsSpellsDoNotGainLifelink() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void sorceryGainsLifeForCreatureAndExcessDamage() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RadiantScrollwielder());
        harness.setHand(player1, List.of(new PigmentStorm()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 19);
        harness.assertNotOnBattlefield(player2, "Radiant Scrollwielder");
    }

    @Test
    void opponentsUpkeepDoesNotExileYourCards() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.findExiledCard(shock.getId())).isNull();
    }

    @Test
    void exiledSorceryRequiresNormalTimingAndMana() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RadiantScrollwielder());
        PigmentStorm storm = new PigmentStorm();
        harness.setGraveyard(player1, List.of(storm));
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, storm.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(storm.getId())).isNotNull();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.castFromExile(player1, storm.getId(), target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(storm.getId())).isNotNull();

        harness.addMana(player1, ManaColor.RED, 5);
        harness.castFromExile(player1, storm.getId(), target.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 19);
        assertThat(gd.findExiledCard(storm.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(storm);
    }

    @Test
    void damageToYourselfWithSpellLifelinkLeavesLifeUnchanged() {
        harness.addToBattlefield(player1, new RadiantScrollwielder());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertLife(player1, 20);
    }
}
