package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElvishRanger;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfTresserhorn.class, ElvishRanger.class, StormCrow.class})
class LordOfTresserhornTest extends BaseCardTest {

    private void castLord() {
        harness.setHand(player1, List.of(new LordOfTresserhorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    @Test
    @DisplayName("ETB loses 2 life, sacrifices two creatures and makes target opponent draw two")
    void etbFullEffect() {
        harness.addToBattlefield(player1, new ElvishRanger());
        harness.addToBattlefield(player1, new StormCrow());
        harness.setLibrary(player2, List.of(new ElvishRanger(), new StormCrow()));
        harness.setHand(player2, List.of());

        castLord();

        harness.assertLife(player1, 18);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent ranger = findPermanent(player1, "Elvish Ranger");
        Permanent crow = findPermanent(player1, "Storm Crow");
        harness.handleMultiplePermanentsChosen(player1, List.of(ranger.getId(), crow.getId()));

        harness.assertNotOnBattlefield(player1, "Elvish Ranger");
        harness.assertNotOnBattlefield(player1, "Storm Crow");
        harness.assertOnBattlefield(player1, "Lord of Tresserhorn");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With no other creatures the Lord sacrifices itself")
    void sacrificesItselfWhenAlone() {
        harness.setLibrary(player2, List.of(new ElvishRanger(), new StormCrow()));
        harness.setHand(player2, List.of());

        castLord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Lord of Tresserhorn");
        harness.assertInGraveyard(player1, "Lord of Tresserhorn");
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("With only one other creature the Lord sacrifices itself and that creature")
    void sacrificesItselfWithOnlyOneOtherCreature() {
        harness.addToBattlefield(player1, new ElvishRanger());
        harness.setLibrary(player2, List.of(new ElvishRanger(), new StormCrow()));
        harness.setHand(player2, List.of());

        castLord();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Lord of Tresserhorn");
        harness.assertNotOnBattlefield(player1, "Elvish Ranger");
        harness.assertInGraveyard(player1, "Lord of Tresserhorn");
        harness.assertInGraveyard(player1, "Elvish Ranger");
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The ETB cannot target its controller")
    void etbRequiresOpponentTarget() {
        LordOfTresserhorn lord = new LordOfTresserhorn();
        harness.setHand(player1, List.of(lord));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lord);
    }

    @Test
    @DisplayName("An uncast Lord ETB still asks for an opponent target")
    void etbChoosesTargetWhenLordEntersWithoutBeingCast() {
        harness.setLibrary(player2, List.of(new ElvishRanger(), new StormCrow()));
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new LordOfTresserhorn());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validPermanentIds()).isEmpty();
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lord of Tresserhorn");
        harness.assertInGraveyard(player1, "Lord of Tresserhorn");
        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("{B} grants a regeneration shield")
    void regenerationAbility() {
        harness.addToBattlefield(player1, new LordOfTresserhorn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent lord = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(lord.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("The regeneration shield saves the Lord from lethal combat damage")
    void regenerationShieldSavesLordFromLethalCombatDamage() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfTresserhorn());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        lord.setBlocking(true);
        lord.addBlockingTarget(0);
        Permanent attacker = new Permanent(new ElvishRanger());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lord of Tresserhorn");
        assertThat(lord.getRegenerationShield()).isZero();
    }
}
