package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.c.CleansingNova;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AjanisLastStand.class, GreenwoodSentinel.class, MindRot.class, Plains.class,
        CleansingNova.class, AjaniWiseCounselor.class})
class AjanisLastStandTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the death trigger sacrifices the enchantment and creates a 4/4 flying Avatar")
    void creatureDeathAcceptCreatesAvatar() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        castNova();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Ajani's Last Stand");
        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertAvatarToken();
    }

    @Test
    @DisplayName("Declining the death trigger keeps the enchantment and creates no token")
    void creatureDeathDeclineDoesNothing() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        castNova();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Ajani's Last Stand");
        harness.assertNotOnBattlefield(player1, "Avatar");
    }

    @Test
    @DisplayName("A planeswalker you control dying also triggers the ability")
    void planeswalkerDeathTriggers() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        Permanent walker = harness.addToBattlefieldAndReturn(player1, new AjaniWiseCounselor());
        walker.setCounterCount(CounterType.LOYALTY, 3);
        walker.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        // -3: loyalty hits 0 and Ajani dies to state-based actions.
        harness.activateAbility(player1, 1, 1, null, null);

        harness.assertInGraveyard(player1, "Ajani, Wise Counselor");
        harness.passBothPriorities(); // resolve the loyalty ability, then the queued may-ability
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertAvatarToken();
    }

    @Test
    @DisplayName("An opponent's creature dying does not trigger the ability")
    void opponentCreatureDeathDoesNotTrigger() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player2, new GreenwoodSentinel());
        castNova();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotOnBattlefield(player1, "Avatar");
    }

    @Test
    @DisplayName("Discarded by an opponent while controlling a Plains creates the Avatar")
    void discardedByOpponentWithPlainsCreatesAvatar() {
        harness.addToBattlefield(player2, new Plains());
        discardToPlayer2();

        harness.assertOnBattlefield(player2, "Avatar");
    }

    @Test
    @DisplayName("Discarded by an opponent without a Plains creates nothing")
    void discardedByOpponentWithoutPlainsDoesNothing() {
        discardToPlayer2();

        harness.assertNotOnBattlefield(player2, "Avatar");
    }

    @Test
    @DisplayName("Discarding without a Plains does not put an ability on the stack")
    void noPlainsAtDiscardDoesNotTrigger() {
        beginDiscardToPlayer2();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gaining a Plains after discarding cannot enable the discard ability")
    void plainsEnteringAfterDiscardDoesNotCreateAvatar() {
        beginDiscardToPlayer2();
        harness.addToBattlefield(player2, new Plains());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Avatar");
    }

    @Test
    @DisplayName("Your own discard spell does not trigger the ability even with a Plains")
    void ownDiscardSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new MindRot(), new AjanisLastStand(), new GreenwoodSentinel()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Avatar");
    }

    @Test
    @DisplayName("Multiple creature deaths cannot sacrifice the same enchantment twice")
    void multipleDeathsCreateOnlyOneAvatar() {
        harness.addToBattlefield(player1, new AjanisLastStand());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        harness.addToBattlefield(player1, new GreenwoodSentinel());
        castNova();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Ajani's Last Stand");
        assertThat(countPermanents(player1, "Avatar")).isEqualTo(1);
    }

    private void discardToPlayer2() {
        beginDiscardToPlayer2();
        harness.passBothPriorities();
    }

    private void beginDiscardToPlayer2() {
        harness.setHand(player2, new ArrayList<>(List.of(new AjanisLastStand(), new GreenwoodSentinel())));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard Ajani's Last Stand
        harness.handleCardChosen(player2, 0); // discard Greenwood Sentinel
    }

    private void castNova() {
        harness.setHand(player1, List.of(new CleansingNova()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities(); // resolve Cleansing Nova - creatures die
        harness.passBothPriorities(); // resolve the death may-effect prompt (if any)
    }

    private void assertAvatarToken() {
        Permanent token = findPermanent(player1, "Avatar");
        assertThat(token).isNotNull();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

}
