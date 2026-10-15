package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.a.AcidSpewerDragon;
import com.github.laxika.magicalvibes.cards.t.Twincast;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FoulTongueInvocation.class, ColossodonYearling.class, AcidSpewerDragon.class, Twincast.class})
class FoulTongueInvocationTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices a creature")
    void sacrificesTargetPlayersCreature() {
        harness.addToBattlefield(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Colossodon Yearling");
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Revealing a Dragon gains 4 life")
    void revealingDragonGainsLife() {
        AcidSpewerDragon dragon = new AcidSpewerDragon();
        harness.addToBattlefield(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new FoulTongueInvocation(), dragon));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Colossodon Yearling");
        harness.assertLife(player1, 14);
        assertThat(gd.playerHands.get(player1.getId())).contains(dragon);
    }

    @Test
    @DisplayName("Controlling a Dragon as cast gains 4 life")
    void controllingDragonAsCastGainsLife() {
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.addToBattlefield(player2, new ColossodonYearling());
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Colossodon Yearling");
        harness.assertLife(player1, 14);
    }

    @Test
    void gainsLifeEvenWhenTargetControlsNoCreatures() {
        harness.setHand(player1, List.of(new FoulTongueInvocation(), new AcidSpewerDragon()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
        harness.assertInHand(player1, "Acid-Spewer Dragon");
    }

    @Test
    void targetingSelfCanSacrificeTheDragonAndStillGainLife() {
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Acid-Spewer Dragon");
        harness.assertLife(player1, 14);
    }

    @Test
    void dragonEnteringAfterCastingDoesNotGrantLife() {
        harness.setHand(player1, List.of(new FoulTongueInvocation()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstant(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.passBothPriorities();

        harness.assertLife(player1, 10);
    }

    @Test
    void revealingAndControllingDragonOnlyGainsFourLife() {
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.setHand(player1, List.of(new FoulTongueInvocation(), new AcidSpewerDragon()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }

    @Test
    void targetedPlayerChoosesCreatureAndLifeGainResumesAfterChoice() {
        harness.addToBattlefield(player2, new ColossodonYearling());
        harness.addToBattlefield(player2, new AcidSpewerDragon());
        var chosen = gd.playerBattlefields.get(player2.getId()).getLast();
        harness.setHand(player1, List.of(new FoulTongueInvocation(), new AcidSpewerDragon()));
        harness.setLife(player1, 10);
        addMana();

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, chosen.getId());

        harness.assertInGraveyard(player2, "Acid-Spewer Dragon");
        harness.assertOnBattlefield(player2, "Colossodon Yearling");
        harness.assertLife(player1, 14);
    }

    @Test
    void copyInheritsDragonRevealAndGainsLifeForCopyController() {
        FoulTongueInvocation invocation = new FoulTongueInvocation();
        harness.setHand(player1, List.of(invocation, new AcidSpewerDragon()));
        harness.setHand(player2, List.of(new Twincast()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstantWithDiscard(player1, 0, player2.getId(), 1);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, invocation.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 14);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
    }
    @Test
    void cannotRevealANonDragonForTheAdditionalCost() {
        harness.setHand(player1, List.of(new FoulTongueInvocation(), new ColossodonYearling()));
        addMana();

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, player2.getId(), 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void copyDoesNotInheritControllingDragonAsCastBonus() {
        FoulTongueInvocation invocation = new FoulTongueInvocation();
        harness.addToBattlefield(player1, new AcidSpewerDragon());
        harness.addToBattlefield(player2, new AcidSpewerDragon());
        harness.setHand(player1, List.of(invocation));
        harness.setHand(player2, List.of(new Twincast()));
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, invocation.getId());
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Acid-Spewer Dragon");
        harness.assertLife(player2, 10);
        harness.passBothPriorities();
        harness.assertLife(player1, 14);
    }
    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
