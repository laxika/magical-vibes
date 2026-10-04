package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
import com.github.laxika.magicalvibes.cards.g.Geistflame;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarvestPyre.class, WalkingCorpse.class, TyphoidRats.class, Geistflame.class})
class HarvestPyreTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Harvest Pyre exiles chosen cards from graveyard and sets X to count")
    void castingExilesCardsAndSetsX() {
        TyphoidRats rats = new TyphoidRats();
        WalkingCorpse corpse = new WalkingCorpse();
        Geistflame geistflame = new Geistflame();
        harness.setGraveyard(player1, List.of(rats, corpse, geistflame));

        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile 2 cards (indices 0 and 1)
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getXValue()).isEqualTo(2); // 2 cards exiled

        // Two cards should be exiled from graveyard, one remains
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can cast Harvest Pyre exiling zero cards (X=0)")
    void canCastWithZeroExiles() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(0);
    }

    @Test
    @DisplayName("Harvest Pyre deals X damage equal to number of exiled cards")
    void dealsDamageEqualToExiledCount() {
        TyphoidRats rats = new TyphoidRats();
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(rats, corpse));

        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile both cards (X=2), enough to kill a 2/2
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Walking Corpse");
        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Harvest Pyre with 1 card exiled deals 1 damage")
    void oneCardExiledDealsOneDamage() {
        TyphoidRats rats = new TyphoidRats();
        harness.setGraveyard(player1, List.of(rats));

        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        // 1 damage doesn't kill a 2/2
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Harvest Pyre with X=0 deals no damage")
    void zeroExilesDealsNoDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of());
        harness.passBothPriorities();

        // 0 damage, creature survives
        harness.assertOnBattlefield(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("Can exile any card type from graveyard (not restricted to creatures)")
    void canExileAnyCardType() {
        Geistflame geistflame = new Geistflame(); // Instant
        harness.setGraveyard(player1, List.of(geistflame));

        Permanent target = harness.addToBattlefieldAndReturn(player2, new TyphoidRats());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Exile the instant (X=1)
        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.passBothPriorities();

        // 1 damage kills a 1/1
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Geistflame"));
    }

    @Test
    @DisplayName("Exile cost is paid even if spell fizzles due to target removal")
    void exileCostPaidEvenIfSpellFizzles() {
        TyphoidRats rats = new TyphoidRats();
        WalkingCorpse corpse = new WalkingCorpse();
        harness.setGraveyard(player1, List.of(rats, corpse));

        Permanent target = harness.addToBattlefieldAndReturn(player2, new TyphoidRats());

        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0, 1));

        // Exile cost already paid
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Cards are still exiled (cost is not refunded)
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Duplicate graveyard selections cannot pay for two damage")
    void cannotExileTheSameCardTwice() {
        TyphoidRats fuel = new TyphoidRats();
        harness.setGraveyard(player1, List.of(fuel));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());
        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithMultipleGraveyardExile(
                player1, 0, target.getId(), List.of(0, 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(fuel);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage uses the paid exile count even when the graveyard changes")
    void damageRemainsFixedAndCanTargetOwnCreature() {
        harness.setGraveyard(player1, List.of(new TyphoidRats()));
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        harness.setHand(player1, List.of(new HarvestPyre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithMultipleGraveyardExile(player1, 0, target.getId(), List.of(0));
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new Geistflame()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Harvest Pyre");
    }
}
