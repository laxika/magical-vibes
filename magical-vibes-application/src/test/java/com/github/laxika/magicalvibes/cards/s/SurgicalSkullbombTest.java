package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSporesinger;
import com.github.laxika.magicalvibes.cards.t.TyvarsStand;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SurgicalSkullbomb.class, AdaptiveSporesinger.class, TyvarsStand.class})
class SurgicalSkullbombTest extends BaseCardTest {

    @Test
    @DisplayName("The basic ability sacrifices Surgical Skullbomb and draws a card")
    void sacrificesAndDraws() {
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new SurgicalSkullbomb());
        AdaptiveSporesinger draw = new AdaptiveSporesinger();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The second ability returns a creature and draws a card")
    void sacrificesReturnsCreatureAndDraws() {
        Permanent skullbomb = harness.addToBattlefieldAndReturn(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        AdaptiveSporesinger draw = new AdaptiveSporesinger();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skullbomb);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(skullbomb.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The second ability can target only a creature")
    void targetMustBeCreature() {
        harness.addToBattlefieldAndReturn(player1, new SurgicalSkullbomb());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new SurgicalSkullbomb());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Surgical Skullbomb");
    }

    @Test
    @DisplayName("The second ability can be activated only at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void basicAbilityWorksDuringOpponentsCombatAndSacrificesImmediately() {
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        AdaptiveSporesinger draw = new AdaptiveSporesinger();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.activateAbility(player1, 0, 0, null);

        harness.assertNotOnBattlefield(player1, "Surgical Skullbomb");
        harness.assertInGraveyard(player1, "Surgical Skullbomb");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    void secondAbilityCannotBeActivatedDuringCombat() {
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Surgical Skullbomb");
    }

    @Test
    void secondAbilityCannotBeActivatedWithAnAbilityOnTheStack() {
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        harness.setLibrary(player1, List.of(new AdaptiveSporesinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Surgical Skullbomb");
    }

    @Test
    void illegalTargetPreventsBothReturnAndDraw() {
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AdaptiveSporesinger());
        AdaptiveSporesinger draw = new AdaptiveSporesinger();
        harness.setLibrary(player1, List.of(draw));
        harness.setHand(player2, List.of(new TyvarsStand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.assertInGraveyard(player1, "Surgical Skullbomb");
        harness.castInstant(player2, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Adaptive Sporesinger");
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void secondAbilityCanReturnYourOwnCreatureDuringPostcombatMain() {
        harness.addToBattlefield(player1, new SurgicalSkullbomb());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AdaptiveSporesinger());
        AdaptiveSporesinger draw = new AdaptiveSporesinger();
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Adaptive Sporesinger");
        harness.assertInGraveyard(player1, "Surgical Skullbomb");
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard(), draw);
    }
}
