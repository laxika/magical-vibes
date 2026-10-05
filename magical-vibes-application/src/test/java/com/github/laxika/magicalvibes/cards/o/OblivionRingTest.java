package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldmeadowStalwart;
import com.github.laxika.magicalvibes.cards.m.MerrowCommerce;
import com.github.laxika.magicalvibes.cards.r.Rootgrapple;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OblivionRing.class, Forest.class, GoldmeadowStalwart.class, MerrowCommerce.class,
        Rootgrapple.class})
class OblivionRingTest extends BaseCardTest {

    private void castAndResolveOblivionRing(UUID targetId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OblivionRing()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities(); // resolve enchantment spell -> ETB on stack
        harness.passBothPriorities(); // resolve ETB -> exile
    }

    private void resetForFollowUpSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesOpponentPermanent() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        UUID stalwartId = harness.getPermanentId(player2, "Goldmeadow Stalwart");
        castAndResolveOblivionRing(stalwartId);

        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("ETB can exile a nonland permanent the controller owns")
    void etbExilesOwnPermanent() {
        harness.addToBattlefield(player1, new MerrowCommerce());
        UUID commerceId = harness.getPermanentId(player1, "Merrow Commerce");
        castAndResolveOblivionRing(commerceId);

        harness.assertNotOnBattlefield(player1, "Merrow Commerce");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Merrow Commerce"));
    }

    @Test
    @DisplayName("ETB still exiles the target if Oblivion Ring leaves before ETB resolves")
    void etbExilesTargetAfterRingLeavesBeforeEtbResolves() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        UUID stalwartId = harness.getPermanentId(player2, "Goldmeadow Stalwart");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OblivionRing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, stalwartId);
        harness.passBothPriorities(); // resolve Oblivion Ring; leave its ETB ability on the stack

        UUID ringId = harness.getPermanentId(player1, "Oblivion Ring");
        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ringId);
        harness.passBothPriorities(); // resolve the empty leave trigger
        harness.passBothPriorities(); // resolve the enter trigger after its source has left

        harness.assertNotOnBattlefield(player1, "Oblivion Ring");
        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled card returns under owner's control when Oblivion Ring is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        UUID stalwartId = harness.getPermanentId(player2, "Goldmeadow Stalwart");
        castAndResolveOblivionRing(stalwartId);

        resetForFollowUpSpell();

        // Destroy Oblivion Ring
        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        UUID ringId = harness.getPermanentId(player1, "Oblivion Ring");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ringId);
        harness.passBothPriorities(); // resolve the return trigger

        // Goldmeadow Stalwart returns under player2's (owner's) control
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Goldmeadow Stalwart"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Exiled permanent returns under its owner's control after being stolen")
    void exiledStolenPermanentReturnsToOwner() {
        Permanent stolen = harness.addToBattlefieldAndReturn(player1, new GoldmeadowStalwart());
        gd.stolenCreatures.put(stolen.getId(), player2.getId());
        castAndResolveOblivionRing(stolen.getId());

        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        UUID ringId = harness.getPermanentId(player1, "Oblivion Ring");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ringId);
        harness.passBothPriorities(); // resolve the return trigger

        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
        harness.assertNotOnBattlefield(player1, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Returned permanent has summoning sickness")
    void returnedPermanentHasSummoningSickness() {
        harness.addToBattlefield(player2, new GoldmeadowStalwart());
        UUID stalwartId = harness.getPermanentId(player2, "Goldmeadow Stalwart");
        castAndResolveOblivionRing(stalwartId);

        resetForFollowUpSpell();

        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        UUID ringId = harness.getPermanentId(player1, "Oblivion Ring");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ringId);
        harness.passBothPriorities(); // resolve the return trigger

        Permanent returned = findPermanent(player2, "Goldmeadow Stalwart");
        assertThat(returned.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Exiled card stays in exile until the separate leave trigger resolves")
    void returnWaitsForLeaveTriggerToResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GoldmeadowStalwart());
        castAndResolveOblivionRing(target.getId());

        resetForFollowUpSpell();
        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        UUID ringId = harness.getPermanentId(player1, "Oblivion Ring");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, ringId);

        harness.assertNotOnBattlefield(player1, "Oblivion Ring");
        harness.assertNotOnBattlefield(player2, "Goldmeadow Stalwart");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Goldmeadow Stalwart"));

        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Goldmeadow Stalwart");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OblivionRing()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enter trigger does nothing when its target leaves before resolution")
    void targetLeavingBeforeEnterTriggerResolvesIsNotExiled() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerrowCommerce());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new OblivionRing()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Rootgrapple()));
        harness.addMana(player2, ManaColor.GREEN, 5);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Oblivion Ring");
        harness.assertNotOnBattlefield(player2, "Merrow Commerce");
        harness.assertInGraveyard(player2, "Merrow Commerce");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
