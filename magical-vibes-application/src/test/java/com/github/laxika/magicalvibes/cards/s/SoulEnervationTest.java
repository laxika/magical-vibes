package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Reminisce;
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

@CardUsed({SoulEnervation.class, ColossalDreadmaw.class, GrizzlyBears.class, Forest.class, Reminisce.class})
class SoulEnervationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives any target creature -4/-4 until end of turn")
    void etbShrinksTargetCreatureUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ColossalDreadmaw());

        harness.setHand(player1, List.of(new SoulEnervation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new SoulEnervation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("One simultaneous creature-card departure makes each opponent lose 1 and you gain 1")
    void triggersOnceForSeveralCreatureCardsLeavingTogether() {
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Does not trigger when only a noncreature card leaves your graveyard")
    void doesNotTriggerForNoncreatureCard() {
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Flash allows casting on an opponent's turn and targeting your own creature")
    void canCastOnOpponentsTurnAndKillOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new SoulEnervation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soul Enervation");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature cards leaving an opponent's graveyard do not trigger the ability")
    void doesNotTriggerForOpponentsGraveyard() {
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Separate creature-card departure events in one turn each trigger")
    void triggersAgainForSeparateDepartureInSameTurn() {
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.setHand(player1, List.of(new Reminisce(), new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);

        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Soul Enervation triggers once for a simultaneous departure")
    void eachCopyTriggersIndependently() {
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.addToBattlefield(player1, new SoulEnervation());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Reminisce()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An illegal ETB target does not remove either enchantment")
    void enchantmentRemainsWhenEtbTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SoulEnervation(), new SoulEnervation()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can enter with no creatures available for the ETB target")
    void canEnterWithoutCreatures() {
        harness.setHand(player1, List.of(new SoulEnervation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soul Enervation");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
