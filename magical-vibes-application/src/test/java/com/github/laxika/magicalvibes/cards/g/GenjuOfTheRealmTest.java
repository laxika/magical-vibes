package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TendoIceBridge;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GenjuOfTheRealm.class, TendoIceBridge.class, GnarledMass.class})
class GenjuOfTheRealmTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted land becomes a legendary 8/12 Spirit with trample while remaining a land")
    void animatesEnchantedLand() {
        Permanent land = addEnchantedLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateGenju(player1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(12);
        assertThat(gqs.effectiveCreatureSubtypes(gd, land)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("The animated land stops being a creature and legendary at end of turn")
    void animationEndsAtEndOfTurn() {
        Permanent land = addEnchantedLand(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        activateGenju(player1);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The Genju controller can activate it on an opponent's land")
    void controllerCanAnimateOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TendoIceBridge());
        GenjuOfTheRealm genju = new GenjuOfTheRealm();
        genju.setOwnerId(player1.getId());
        harness.setHand(player1, List.of(genju));
        addGenjuMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        activateGenju(player1);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(12);
        assertThat(gqs.effectiveCreatureSubtypes(gd, land)).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasEffectiveSupertype(gd, land, CardSupertype.LEGENDARY)).isTrue();
    }

    @Test
    @DisplayName("When the enchanted land dies, Genju may return from the graveyard to its owner's hand")
    void returnsToHandWhenEnchantedLandDies() {
        addEnchantedLand(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Tendo Ice Bridge")));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Genju of the Realm");
        harness.assertNotInGraveyard(player1, "Genju of the Realm");
        harness.assertNotOnBattlefield(player1, "Genju of the Realm");
    }

    @Test
    @DisplayName("Genju stays in the graveyard when its land death trigger is declined")
    void decliningReturnLeavesGenjuInGraveyard() {
        addEnchantedLand(player1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(
                gd, findPermanent(player1, "Tendo Ice Bridge")));
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Genju of the Realm");
        harness.assertNotInHand(player1, "Genju of the Realm");
    }

    @Test
    @DisplayName("Genju can enchant only a land")
    void cannotEnchantNonland() {
        harness.addToBattlefield(player1, new GnarledMass());
        Permanent creature = findPermanent(player1, "Gnarled Mass");
        harness.setHand(player1, List.of(new GenjuOfTheRealm()));
        addGenjuMana(player1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addEnchantedLand(Player controller) {
        Permanent land = harness.addToBattlefieldAndReturn(controller, new TendoIceBridge());
        GenjuOfTheRealm genju = new GenjuOfTheRealm();
        genju.setOwnerId(controller.getId());
        harness.setHand(controller, List.of(genju));
        addGenjuMana(controller);
        harness.forceActivePlayer(controller);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(controller, 0, land.getId());
        harness.passBothPriorities();
        return land;
    }

    private void activateGenju(Player controller) {
        int genjuIndex = gd.playerBattlefields.get(controller.getId()).indexOf(
                findPermanent(controller, "Genju of the Realm"));
        harness.activateAbility(controller, genjuIndex, 0, null, null);
    }

    private void addGenjuMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
