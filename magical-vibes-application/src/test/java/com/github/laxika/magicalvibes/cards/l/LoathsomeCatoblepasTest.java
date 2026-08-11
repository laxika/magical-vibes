package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Ulcerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoathsomeCatoblepasTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability makes Loathsome Catoblepas must be blocked")
    void activatedAbilityMakesSourceMustBeBlocked() {
        harness.addToBattlefield(player1, new LoathsomeCatoblepas());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent catoblepas = findPermanent(player1, "Loathsome Catoblepas");
        assertThat(catoblepas.isMustBeBlockedThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Must-be-blocked requirement wears off at end of turn")
    void mustBeBlockedRequirementWearsOff() {
        harness.addToBattlefield(player1, new LoathsomeCatoblepas());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent catoblepas = findPermanent(player1, "Loathsome Catoblepas");
        assertThat(catoblepas.isMustBeBlockedThisTurn()).isFalse();
    }

    @Test
    @DisplayName("When Loathsome Catoblepas dies, an opponent creature gets -3/-3")
    void deathTriggerShrinksOpponentCreature() {
        harness.addToBattlefield(player1, new LoathsomeCatoblepas());
        UUID catoblepasId = harness.getPermanentId(player1, "Loathsome Catoblepas");

        GrizzlyBears target = new GrizzlyBears();
        target.setPower(4);
        target.setToughness(4);
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killCatoblepas(catoblepasId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        Permanent targetPermanent = findPermanent(player2, "Grizzly Bears");
        assertThat(targetPermanent.getPowerModifier()).isEqualTo(-3);
        assertThat(targetPermanent.getToughnessModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Death trigger cannot target a creature controlled by its controller")
    void deathTriggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new LoathsomeCatoblepas());
        UUID catoblepasId = harness.getPermanentId(player1, "Loathsome Catoblepas");
        harness.addToBattlefield(player1, new GrizzlyBears());

        killCatoblepas(catoblepasId);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPermanent(player1, "Grizzly Bears").getPowerModifier()).isZero();
        assertThat(findPermanent(player1, "Grizzly Bears").getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Death trigger kills a 3/3 opponent creature")
    void deathTriggerKillsThreeThreeCreature() {
        harness.addToBattlefield(player1, new LoathsomeCatoblepas());
        UUID catoblepasId = harness.getPermanentId(player1, "Loathsome Catoblepas");

        GrizzlyBears target = new GrizzlyBears();
        target.setPower(3);
        target.setToughness(3);
        harness.addToBattlefield(player2, target);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        killCatoblepas(catoblepasId);

        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    private void killCatoblepas(UUID catoblepasId) {
        harness.setHand(player1, List.of(new Ulcerate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0, catoblepasId);
        harness.passBothPriorities();
    }
}
