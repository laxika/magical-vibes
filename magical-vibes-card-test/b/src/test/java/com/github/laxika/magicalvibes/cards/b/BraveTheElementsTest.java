package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({BraveTheElements.class, SuntailHawk.class, GiantSpider.class, Shock.class})
class BraveTheElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Only white creatures you control gain protection from the chosen color")
    void grantsProtectionToOwnWhiteCreaturesOnly() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opponentHawk = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
        assertThat(spider.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
        assertThat(opponentHawk.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionWearsOff() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "BLACK");

        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("With no white creatures the spell still requires its color choice")
    void stillChoosesColorWithoutWhiteCreatures() {
        harness.addToBattlefield(player1, new GiantSpider());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");
    }

    @Test
    @DisplayName("White creatures entering before resolution receive protection")
    void includesWhiteCreaturesPresentAtResolution() {
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0);

        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(hawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("White creatures entering after resolution do not receive protection")
    void excludesWhiteCreaturesEnteringAfterResolution() {
        Permanent originalHawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "WHITE");

        Permanent laterHawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(originalHawk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);
        assertThat(laterHawk.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Protection makes a pending red damage spell's target illegal")
    void protectsAgainstShockAlreadyOnStack() {
        Permanent hawk = harness.addToBattlefieldAndReturn(player1, new SuntailHawk());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new BraveTheElements()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player2, 0, hawk.getId());
        harness.castAndResolveInstant(player1, 0);
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(hawk);
        assertThat(hawk.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
