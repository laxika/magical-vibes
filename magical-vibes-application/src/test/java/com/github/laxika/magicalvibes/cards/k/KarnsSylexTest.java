package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AdantoVanguard;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InscribedTablet;
import com.github.laxika.magicalvibes.cards.m.MyrConvert;
import com.github.laxika.magicalvibes.cards.v.ViciousRivalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        KarnsSylex.class,
        AdantoVanguard.class,
        Forest.class,
        GrizzlyBears.class,
        HillGiant.class,
        InscribedTablet.class,
        MyrConvert.class,
        ViciousRivalry.class
})
class KarnsSylexTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and destroys nonland permanents within X after exiling itself")
    void entersTappedAndDestroysNonlandsWithinX() {
        Permanent sylex = harness.enterBattlefieldAndReturn(player1, new KarnsSylex());
        assertThat(sylex.isTapped()).isTrue();
        sylex.untap();
        sylex.setSummoningSick(false);
        Permanent smallCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largeCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player1, new Forest());
        Permanent opposingSmallCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceSorcerySpeed();

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(smallCreature.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingSmallCreature.getCard());
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Karn's Sylex");
    }

    @Test
    @DisplayName("Prevents life-payment spell and non-mana ability costs")
    void preventsLifePaymentCosts() {
        addSylexReady();
        Permanent vanguard = addCreatureReady(player1, new AdantoVanguard());
        harness.setLife(player1, 20);
        forceSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(vanguard), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't pay life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't pay life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Still allows a life payment in a mana ability")
    void allowsManaAbilityLifePayment() {
        addSylexReady();
        Permanent myr = addCreatureReady(player1, new MyrConvert());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(myr), null, null);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent sylex = harness.enterBattlefieldAndReturn(player1, new KarnsSylex());
        forceSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sylex.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Karn's Sylex");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate outside a main phase or during an opponent's turn")
    void requiresOwnMainPhase() {
        addSylexReady();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Karn's Sylex");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Zero-life spell costs remain payable, but Sylex cannot activate with that spell on the stack")
    void allowsZeroLifePaymentAndRequiresEmptyStack() {
        addSylexReady();
        forceSorcerySpeed();
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new ViciousRivalry()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Karn's Sylex");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Vicious Rivalry");
        harness.assertOnBattlefield(player1, "Karn's Sylex");
    }

    @Test
    @DisplayName("X zero exiles Sylex without destroying positive-mana-value permanents or lands")
    void canActivateWithZeroX() {
        Permanent sylex = addSylexReady();
        harness.addToBattlefield(player1, new InscribedTablet());
        harness.addToBattlefield(player2, new Forest());
        forceSorcerySpeed();

        harness.activateAbility(player1, 0, 0, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex.getCard());
        harness.assertOnBattlefield(player1, "Inscribed Tablet");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inscribed Tablet");
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInGraveyard(player1, "Karn's Sylex");
    }

    @Test
    @DisplayName("Destroys noncreature artifacts at the X boundary on both battlefields")
    void destroysNoncreatureArtifacts() {
        addSylexReady();
        harness.addToBattlefield(player1, new InscribedTablet());
        harness.addToBattlefield(player2, new InscribedTablet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        forceSorcerySpeed();

        harness.activateAbility(player1, 0, 1, null);

        harness.assertOnBattlefield(player1, "Inscribed Tablet");
        harness.assertOnBattlefield(player2, "Inscribed Tablet");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Inscribed Tablet");
        harness.assertInGraveyard(player2, "Inscribed Tablet");
        harness.assertNotOnBattlefield(player1, "Inscribed Tablet");
        harness.assertNotOnBattlefield(player2, "Inscribed Tablet");
    }

    @Test
    @DisplayName("Restriction affects opponents and ends immediately when Sylex is exiled as a cost")
    void restrictionEndsBeforeAbilityResolvesAndIndestructibleSurvives() {
        Permanent sylex = addSylexReady();
        harness.addToBattlefield(player2, new AdantoVanguard());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceSorcerySpeed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't pay life");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        harness.activateAbility(player1, 0, 2, null);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sylex.getCard());
        harness.assertOnBattlefield(player2, "Adanto Vanguard");
        harness.activateAbility(player2, 0, null, null);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Adanto Vanguard");
        harness.assertNotInGraveyard(player2, "Adanto Vanguard");
        harness.assertNotOnBattlefield(player1, "Karn's Sylex");
    }

    private Permanent addSylexReady() {
        Permanent sylex = harness.addToBattlefieldAndReturn(player1, new KarnsSylex());
        sylex.untap();
        sylex.setSummoningSick(false);
        return sylex;
    }

    private void forceSorcerySpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
