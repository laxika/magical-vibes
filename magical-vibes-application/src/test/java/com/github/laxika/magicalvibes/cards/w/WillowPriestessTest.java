package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.FolkOfAnHavva;
import com.github.laxika.magicalvibes.cards.r.Roterothopter;
import com.github.laxika.magicalvibes.cards.t.Torture;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WillowPriestess.class, WillowFaerie.class, FolkOfAnHavva.class, Roterothopter.class, Torture.class})
class WillowPriestessTest extends BaseCardTest {

    @Test
    @DisplayName("{T}: puts a Faerie permanent card from hand onto the battlefield")
    void putsFaerieFromHandOntoBattlefield() {
        Permanent priestess = addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new WillowFaerie()));

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(priestess.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Willow Faerie");
        harness.assertNotInHand(player1, "Willow Faerie");
    }

    @Test
    @DisplayName("Declining the may leaves the Faerie card in hand")
    void decliningLeavesFaerieInHand() {
        addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new WillowFaerie()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Willow Faerie");
        harness.assertInHand(player1, "Willow Faerie");
    }

    @Test
    @DisplayName("The tap ability cannot put a non-Faerie permanent from hand onto the battlefield")
    void doesNotPutNonFaerieFromHand() {
        addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new FolkOfAnHavva()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Folk of An-Havva");
        harness.assertInHand(player1, "Folk of An-Havva");
    }

    @Test
    @DisplayName("{2}{G}: target green creature gains protection from black until end of turn")
    void grantsProtectionFromBlack() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new FolkOfAnHavva());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID folkId = harness.getPermanentId(player1, "Folk of An-Havva");
        harness.activateAbility(player1, 0, 1, null, folkId);
        harness.passBothPriorities();

        Permanent folk = findPermanent(player1, "Folk of An-Havva");
        assertThat(folk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("The protection ability can target a green creature an opponent controls")
    void grantsProtectionToOpponentsGreenCreature() {
        addCreatureReady(player1, new WillowPriestess());
        addCreatureReady(player2, new FolkOfAnHavva());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID folkId = harness.getPermanentId(player2, "Folk of An-Havva");
        harness.activateAbility(player1, 0, 1, null, folkId);
        harness.passBothPriorities();

        Permanent folk = findPermanent(player2, "Folk of An-Havva");
        assertThat(folk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Granted protection expires at the end of the turn")
    void protectionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new WillowPriestess());
        addCreatureReady(player1, new FolkOfAnHavva());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID folkId = harness.getPermanentId(player1, "Folk of An-Havva");
        harness.activateAbility(player1, 0, 1, null, folkId);
        harness.passBothPriorities();

        Permanent folk = findPermanent(player1, "Folk of An-Havva");
        assertThat(folk.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(folk.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("The granted protection makes the creature an illegal target for a black spell")
    void protectionStopsBlackRemoval() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new FolkOfAnHavva());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID folkId = harness.getPermanentId(player1, "Folk of An-Havva");
        harness.activateAbility(player1, 0, 1, null, folkId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Torture()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castEnchantment(player2, 0, folkId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Folk of An-Havva");
    }

    @Test
    @DisplayName("Cannot target a creature that isn't green")
    void cannotTargetNonGreenCreature() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new Roterothopter());
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID thopterId = harness.getPermanentId(player1, "Roterothopter");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, thopterId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The tap ability puts only the chosen Faerie onto the battlefield")
    void putsOnlyOneFaerieFromMixedHand() {
        addCreatureReady(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new FolkOfAnHavva(), new WillowFaerie(), new WillowPriestess()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(countPermanents(player1, "Willow Faerie")).isEqualTo(1);
        assertThat(countPermanents(player1, "Willow Priestess")).isEqualTo(1);
        harness.assertInHand(player1, "Willow Priestess");
        harness.assertInHand(player1, "Folk of An-Havva");
        assertThat(findPermanent(player1, "Willow Faerie").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick Priestess cannot activate its tap ability")
    void summoningSicknessPreventsTapAbility() {
        harness.addToBattlefield(player1, new WillowPriestess());
        harness.setHand(player1, List.of(new WillowFaerie()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Willow Faerie");
    }

    @Test
    @DisplayName("A tapped Priestess cannot activate its tap ability again")
    void tappedPriestessCannotActivateTapAbility() {
        Permanent priestess = addCreatureReady(player1, new WillowPriestess());
        priestess.tap();
        harness.setHand(player1, List.of(new WillowFaerie()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Willow Faerie");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Priestess can grant protection to itself")
    void protectionAbilityNeedsNeitherUntappedSourceNorHaste() {
        harness.addToBattlefield(player1, new WillowPriestess());
        Permanent priestess = findPermanent(player1, "Willow Priestess");
        priestess.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, priestess.getId());
        harness.passBothPriorities();

        assertThat(priestess.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
        assertThat(priestess.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Protection makes a black Aura already on the stack lose its target")
    void protectionInResponseStopsBlackAura() {
        addCreatureReady(player1, new WillowPriestess());
        harness.addToBattlefield(player1, new FolkOfAnHavva());
        UUID folkId = harness.getPermanentId(player1, "Folk of An-Havva");
        harness.setHand(player2, List.of(new Torture()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castEnchantment(player2, 0, folkId);

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, 1, null, folkId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Folk of An-Havva");
        harness.assertNotOnBattlefield(player2, "Torture");
        harness.assertInGraveyard(player2, "Torture");
    }
}
