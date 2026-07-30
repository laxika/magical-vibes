package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.i.IslandSanctuary;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GhostlyFlickerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles and immediately returns both targeted permanents")
    void flickersBothTargets() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.castInstant(player1, 0, List.of(bearsId, islandId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        // Both come back as new objects.
        assertThat(harness.getPermanentId(player1, "Grizzly Bears")).isNotEqualTo(bearsId);
        assertThat(harness.getPermanentId(player1, "Island")).isNotEqualTo(islandId);
        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Both returned permanents trigger their enter-the-battlefield abilities")
    void retriggersEnterTheBattlefieldAbilities() {
        harness.addToBattlefield(player1, new AngelOfMercy());
        harness.addToBattlefield(player1, new GoldnightRedeemer());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID angelId = harness.getPermanentId(player1, "Angel of Mercy");
        UUID redeemerId = harness.getPermanentId(player1, "Goldnight Redeemer");

        harness.castInstant(player1, 0, List.of(angelId, redeemerId));
        harness.passBothPriorities();
        // Resolve both enter-the-battlefield triggers put on the stack by the returns.
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Angel of Mercy gains 3 life; Goldnight Redeemer gains 2 per other creature you control,
        // and because both are exiled before either returns, each sees the other back on the
        // battlefield: 3 + 2 = 5.
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentPermanent() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID opponentBearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(ownBearsId, opponentBearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land you control");
    }

    @Test
    @DisplayName("Cannot target an enchantment you control")
    void cannotTargetEnchantment() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new IslandSanctuary());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID sanctuaryId = harness.getPermanentId(player1, "Island Sanctuary");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, sanctuaryId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or land you control");
    }

    @Test
    @DisplayName("Cannot target the same permanent twice")
    void cannotTargetSamePermanentTwice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Cannot cast with only one target")
    void cannotCastWithOneTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Still flickers the remaining target when the other has left the battlefield")
    void flickersRemainingTargetWhenOtherIsGone() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID islandId = harness.getPermanentId(player1, "Island");

        harness.castInstant(player1, 0, List.of(bearsId, islandId));

        Permanent bearsPerm = gqs.findPermanentById(gd, bearsId);
        gd.playerBattlefields.get(player1.getId()).remove(bearsPerm);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Island");
        assertThat(harness.getPermanentId(player1, "Island")).isNotEqualTo(islandId);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Goes to the graveyard after resolving")
    void goesToGraveyardAfterResolution() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GhostlyFlicker()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player1, "Island")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ghostly Flicker");
    }
}
