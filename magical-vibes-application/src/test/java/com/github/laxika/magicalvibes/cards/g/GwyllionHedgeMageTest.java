package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GwyllionHedgeMage.class, Plains.class, Swamp.class, GrizzlyBears.class})
class GwyllionHedgeMageTest extends BaseCardTest {


    @Test
    @DisplayName("With two Plains, ETB may create a 1/1 white Kithkin Soldier token")
    void plainsGateCreatesToken() {
        addLands(player1, 2, 0);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> token ETB on stack
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Plains trigger creates no token")
    void plainsGateDeclinedCreatesNoToken() {
        addLands(player1, 2, 0);
        castGwyllion();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }

    @Test
    @DisplayName("With only one Plains the token trigger does not fire")
    void onePlainsDoesNotTrigger() {
        addLands(player1, 1, 0);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }


    @Test
    @DisplayName("With two Swamps, ETB may put a -1/-1 counter on target creature")
    void swampGatePutsMinusCounter() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> trigger-time target prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities(); // resolve ConditionalEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the Swamp trigger leaves the target creature unchanged")
    void swampGateDeclinedLeavesTargetUnchanged() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("With only one Swamp the -1/-1 trigger does not fire (no target prompt)")
    void oneSwampDoesNotTrigger() {
        addLands(player1, 0, 1);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }


    @Test
    @DisplayName("With no Plains or Swamps, neither ability triggers")
    void neitherGateTriggers() {
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Gwyllion Hedge-Mage");
    }


    @Test
    @DisplayName("With two Plains and two Swamps, both abilities may resolve")
    void bothGatesResolve() {
        addLands(player1, 2, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities(); // resolve creature spell -> target prompt for -1/-1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, bears.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Swamp ability can target either player's creatures, including its source, but no lands")
    void swampTriggerOnlyOffersCreatures() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(
                bears.getId(), harness.getPermanentId(player1, "Gwyllion Hedge-Mage"));
        assertThat(choice.validPlayerIds()).isEmpty();
    }

    @Test
    @DisplayName("The Plains condition is checked again when the ability resolves")
    void losingAPlainsBeforeResolutionPreventsToken() {
        addLands(player1, 2, 0);
        castGwyllion();
        harness.passBothPriorities();
        UUID plainsId = harness.getPermanentId(player1, "Plains");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(plainsId));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }

    @Test
    @DisplayName("The Swamp condition is checked again when the ability resolves")
    void losingASwampBeforeResolutionPreventsCounter() {
        addLands(player1, 0, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        UUID swampId = harness.getPermanentId(player1, "Swamp");
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getId().equals(swampId));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Losing the counter target does not stop the independent token ability")
    void tokenAbilitySurvivesCounterTargetLeaving() {
        addLands(player1, 2, 2);
        Permanent bears = addBears(player2);
        castGwyllion();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);

        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() == null && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(countKithkinSoldierTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counter ability may target Gwyllion Hedge-Mage itself")
    void swampTriggerCanCounterItsSource() {
        addLands(player1, 0, 2);
        castGwyllion();
        harness.passBothPriorities();
        UUID sourceId = harness.getPermanentId(player1, "Gwyllion Hedge-Mage");
        harness.handlePermanentChosen(player1, sourceId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent source = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(sourceId)).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Plains and Swamps do not satisfy either condition")
    void opponentsLandsDoNotEnableTriggers() {
        addLands(player2, 2, 2);
        castGwyllion();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(countKithkinSoldierTokens(player1)).isZero();
    }

    private void castGwyllion() {
        harness.setHand(player1, List.of(new GwyllionHedgeMage()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castCreature(player1, 0);
    }

    private void addLands(Player player, int plains, int swamps) {
        for (int i = 0; i < plains; i++) {
            harness.addToBattlefield(player, new Plains());
        }
        for (int i = 0; i < swamps; i++) {
            harness.addToBattlefield(player, new Swamp());
        }
    }

    private Permanent addBears(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private int countKithkinSoldierTokens(Player player) {
        return (int) gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Kithkin Soldier"))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.KITHKIN))
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.SOLDIER))
                .count();
    }
}
