package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KiraGreatGlassSpinner.class, FirstVolley.class, Frostling.class})
class KiraGreatGlassSpinnerTest extends BaseCardTest {

    private UUID addKira() {
        return addCreatureReady(player1, new KiraGreatGlassSpinner()).getId();
    }

    private UUID addFrostling() {
        return addCreatureReady(player1, new Frostling()).getId();
    }

    @Test
    @DisplayName("Counters the first spell targeting another creature you control")
    void countersFirstSpellTargetingOtherCreature() {
        addKira();
        UUID frostlingId = addFrostling();

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, frostlingId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player2, "First Volley");
    }

    @Test
    @DisplayName("Kira grants the ability to itself as well")
    void countersSpellTargetingKira() {
        UUID kiraId = addKira();

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, kiraId);

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kira, Great Glass-Spinner");
        harness.assertInGraveyard(player2, "First Volley");
    }

    @Test
    @DisplayName("A second spell targeting the same creature that turn resolves")
    void secondSpellSameTurnResolves() {
        addKira();
        UUID frostlingId = addFrostling();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, frostlingId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Frostling");

        harness.castInstant(player2, 0, frostlingId);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player1, "Frostling");
    }

    @Test
    @DisplayName("Each creature has its own first-target protection each turn")
    void eachCreatureHasIndependentProtection() {
        addKira();
        UUID firstFrostlingId = addFrostling();
        UUID secondFrostlingId = addFrostling();

        harness.setHand(player2, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, firstFrostlingId);
        harness.passBothPriorities();
        harness.castInstant(player2, 0, secondFrostlingId);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Frostling")).isEqualTo(2);
    }

    @Test
    @DisplayName("Counters an activated ability targeting a creature you control")
    void countersActivatedAbilityTargetingCreature() {
        addKira();
        UUID frostlingId = addFrostling();
        harness.addToBattlefield(player2, new Frostling());

        harness.activateAbility(player2, 0, null, frostlingId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player2, "Frostling");
    }

    @Test
    @DisplayName("Counters your own spell targeting a creature you control")
    void countersOwnSpellTargetingYourCreature() {
        addKira();
        UUID frostlingId = addFrostling();

        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, frostlingId);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Frostling");
        harness.assertInGraveyard(player1, "First Volley");
    }

    @Test
    @DisplayName("Opponent's creatures do not get the ability")
    void opponentCreaturesUnaffected() {
        addKira();
        harness.addToBattlefield(player2, new Frostling());
        UUID theirFrostlingId = findPermanent(player2, "Frostling").getId();

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, theirFrostlingId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Frostling");
    }

    @Test
    @DisplayName("The grant ends when Kira leaves the battlefield")
    void grantEndsWhenKiraLeaves() {
        addKira();
        UUID frostlingId = addFrostling();

        var kira = findPermanent(player1, "Kira, Great Glass-Spinner");
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, kira));

        harness.setHand(player2, List.of(new FirstVolley()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, frostlingId);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Frostling");
    }
}
