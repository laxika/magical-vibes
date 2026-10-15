package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PriestOfForgottenGods.class, FeralMaaka.class})
class PriestOfForgottenGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Target player loses life, sacrifices a creature, and you get mana and a card")
    void targetPlayerLosesLifeSacrificesCreatureAndControllerGetsManaAndCard() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player2, new FeralMaaka());
        harness.setLibrary(player1, List.of(new FeralMaaka()));

        int targetLifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(targetLifeBefore - 2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInHand(player1, "Feral Maaka");
        harness.assertNotOnBattlefield(player2, "Feral Maaka");
        harness.assertInGraveyard(player2, "Feral Maaka");
    }

    @Test
    @DisplayName("Each target player loses life and the controller can sacrifice the Priest")
    void eachTargetPlayerLosesLife() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player2, new FeralMaaka());
        harness.setLibrary(player1, List.of(new FeralMaaka()));

        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player1.getId(), player2.getId()));
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore - 2);
        harness.assertNotOnBattlefield(player1, "Priest of Forgotten Gods");
        harness.assertInGraveyard(player1, "Priest of Forgotten Gods");
        harness.assertInHand(player1, "Feral Maaka");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 2);
        harness.assertNotOnBattlefield(player2, "Feral Maaka");
    }

    @Test
    @DisplayName("The ability can resolve with no targets and still gives mana and a card")
    void resolvesWithNoTargets() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent firstCostCreature = addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());
        harness.setLibrary(player1, List.of(new FeralMaaka()));

        int opponentLifeBefore = gd.getLife(player2.getId());
        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.handlePermanentChosen(player1, firstCostCreature.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
        harness.assertInHand(player1, "Feral Maaka");
    }

    @Test
    @DisplayName("A target without creatures still loses life and the remaining effects resolve")
    void targetWithoutCreaturesStillLosesLife() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent first = addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());
        harness.setLibrary(player1, List.of(new FeralMaaka()));

        int lifeBefore = gd.getLife(player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertInHand(player1, "Feral Maaka");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The targeted player chooses which creature to sacrifice before mana and draw")
    void targetChoosesCreatureAndResolutionResumes() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        Permanent first = addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());
        Permanent survivor = addCreatureReady(player2, new FeralMaaka());
        Permanent sacrificed = addCreatureReady(player2, new FeralMaaka());
        harness.setLibrary(player1, List.of(new FeralMaaka()));

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.handlePermanentChosen(player2, sacrificed.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(survivor);
        harness.assertInGraveyard(player2, "Feral Maaka");
        harness.assertInHand(player1, "Feral Maaka");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Priest cannot count itself toward the two other creatures cost")
    void requiresTwoOtherCreatures() {
        addCreatureReady(player1, new PriestOfForgottenGods());
        addCreatureReady(player1, new FeralMaaka());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Priest of Forgotten Gods");
        harness.assertOnBattlefield(player1, "Feral Maaka");
    }

    @Test
    @DisplayName("Summoning sickness prevents activating the tap ability")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new PriestOfForgottenGods());
        addCreatureReady(player1, new FeralMaaka());
        addCreatureReady(player1, new FeralMaaka());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Feral Maaka")).isEqualTo(2);
    }
}
