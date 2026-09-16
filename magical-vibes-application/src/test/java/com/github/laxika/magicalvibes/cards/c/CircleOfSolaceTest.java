package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EmbermageGoblin;
import com.github.laxika.magicalvibes.cards.g.GoblinSharpshooter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfSolace.class, EmbermageGoblin.class, GoblinSharpshooter.class})
class CircleOfSolaceTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a creature type as Circle of Solace enters controls its prevention ability")
    void choosesCreatureTypeAsItEnters() {
        Permanent wizard = addCreatureReady(player2, new EmbermageGoblin());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CircleOfSolace(), "{3}{W}");

        harness.passBothPriorities();
        harness.handleListChoice(player1, "WIZARD");

        Permanent circle = findPermanent(player1, "Circle of Solace");
        assertThat(circle.getChosenSubtype()).isEqualTo(CardSubtype.WIZARD);

        activatePrevention(circle);
        activateDamage(wizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Only the next damage from a matching creature is prevented")
    void onlyNextMatchingDamageIsPrevented() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        Permanent firstWizard = addCreatureReady(player2, new EmbermageGoblin());
        Permanent secondWizard = addCreatureReady(player2, new EmbermageGoblin());

        activatePrevention(circle);
        activateDamage(firstWizard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        activateDamage(secondWizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Damage from a different creature type is not prevented")
    void differentCreatureTypeDealsDamageNormally() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        Permanent nonWizard = addCreatureReady(player2, new GoblinSharpshooter());
        Permanent wizard = addCreatureReady(player2, new EmbermageGoblin());

        activatePrevention(circle);
        activateDamage(nonWizard);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        activateDamage(wizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Only damage dealt to you is prevented")
    void onlyPreventsDamageToController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        Permanent firstWizard = addCreatureReady(player2, new EmbermageGoblin());
        Permanent secondWizard = addCreatureReady(player2, new EmbermageGoblin());

        activatePrevention(circle);
        activateDamage(firstWizard, player2.getId());
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        activateDamage(secondWizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Prevents combat damage from a creature of the chosen type")
    void preventsCombatDamageFromMatchingCreature() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        addCreatureReady(player2, new EmbermageGoblin());

        activatePrevention(circle);
        declareAttackers(player2, List.of(0));
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The prevention shield expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        Permanent circle = addCircle(player1, CardSubtype.WIZARD);
        Permanent wizard = addCreatureReady(player2, new EmbermageGoblin());

        activatePrevention(circle);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        activateDamage(wizard);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    private Permanent addCircle(Player player, CardSubtype chosenSubtype) {
        Permanent circle = harness.addToBattlefieldAndReturn(player, new CircleOfSolace());
        circle.setChosenSubtype(chosenSubtype);
        return circle;
    }

    private void activatePrevention(Permanent circle) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(circle), null, null);
        harness.passBothPriorities();
    }

    private void activateDamage(Permanent source) {
        activateDamage(source, player1.getId());
    }

    private void activateDamage(Permanent source, UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(source), null, targetId);
        harness.passBothPriorities();
    }
}
