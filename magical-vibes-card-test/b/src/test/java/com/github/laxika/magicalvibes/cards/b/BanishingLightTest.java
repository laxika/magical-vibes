package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BanishingLight.class, Forest.class, GrizzlyBears.class, Naturalize.class})
class BanishingLightTest extends BaseCardTest {

    private void setUpCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BanishingLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void castAndResolve(UUID targetId) {
        setUpCast();
        harness.castEnchantment(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles target nonland permanent an opponent controls")
    void etbExilesOpponentPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
    }

    @Test
    @DisplayName("Exiled card returns when Banishing Light is destroyed")
    void exiledCardReturnsWhenSourceDestroyed() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        castAndResolve(bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID banishingLightId = harness.getPermanentId(player1, "Banishing Light");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, banishingLightId);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        harness.addToBattlefield(player2, new Forest());
        UUID forestId = harness.getPermanentId(player2, "Forest");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent the caster controls")
    void cannotTargetOwnPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        setUpCast();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, ownBearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Leaving before the enters trigger resolves prevents exile")
    void sourceDestroyedBeforeTriggerResolves() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID lightId = harness.getPermanentId(player1, "Banishing Light");
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, lightId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Banishing Light");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can exile an opposing enchantment")
    void exilesOpponentEnchantment() {
        UUID opposingLightId = harness.addToBattlefieldAndReturn(player2, new BanishingLight()).getId();
        castAndResolve(opposingLightId);

        harness.assertNotOnBattlefield(player2, "Banishing Light");
        harness.assertOnBattlefield(player1, "Banishing Light");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Banishing Light"));
    }

    @Test
    @CardUsed({FlickerOfFate.class})
    @DisplayName("Returning Banishing Light is a new source for its enters trigger")
    void blinkedSourceDoesNotExileOriginalTarget() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        UUID opposingLightId = harness.addToBattlefieldAndReturn(player2, new BanishingLight()).getId();
        setUpCast();
        harness.castEnchantment(player1, 0, bearsId);
        harness.passBothPriorities();

        UUID originalLightId = harness.getPermanentId(player1, "Banishing Light");
        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, originalLightId);
        harness.handlePermanentChosen(player1, opposingLightId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Banishing Light")).isNotEqualTo(originalLightId);
        harness.assertNotOnBattlefield(player2, "Banishing Light");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
