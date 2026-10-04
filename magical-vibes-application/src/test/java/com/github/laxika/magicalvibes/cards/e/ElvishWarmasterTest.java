package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeskirShieldmate;
import com.github.laxika.magicalvibes.cards.j.JasperaSentinel;
import com.github.laxika.magicalvibes.cards.t.TyvarKell;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishWarmaster.class, JasperaSentinel.class, BeskirShieldmate.class, TyvarKell.class})
class ElvishWarmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates an Elf Warrior when another Elf enters, only once each turn")
    void createsTokenOncePerTurnForElfEntry() {
        harness.setHand(player1, List.of(new ElvishWarmaster()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();

        harness.setHand(player1, List.of(new JasperaSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new JasperaSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Activated ability boosts all Elves you control and grants them deathtouch")
    void activatedAbilityBoostsElvesAndGrantsDeathtouch() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addToBattlefield(player1, new JasperaSentinel());
        harness.addToBattlefield(player1, new BeskirShieldmate());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent warmaster = findPermanent(player1, "Elvish Warmaster");
        Permanent elf = findPermanent(player1, "Jaspera Sentinel");
        Permanent nonElf = findPermanent(player1, "Beskir Shieldmate");
        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, elf, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonElf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, nonElf)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, nonElf, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Activated ability's boost and deathtouch wear off at end of turn")
    void activatedAbilityWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent warmaster = findPermanent(player1, "Elvish Warmaster");
        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warmaster)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void nonElfEntryDoesNotConsumeTheTrigger() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.setHand(player1, List.of(new BeskirShieldmate(), new JasperaSentinel()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(1);
    }

    @Test
    void opponentsElfDoesNotTriggerWarmaster() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new JasperaSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    void triggerResetsOnTheNextTurnAndAcceptsElfTokens() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addToBattlefield(player1, new TyvarKell());
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(4);
    }

    @Test
    void eachWarmasterTriggersIndependentlyWithoutTokenRecursion() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.setHand(player1, List.of(new JasperaSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void activatedAbilityDoesNotBoostOpponentsOrElvesEnteringLater() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addToBattlefield(player2, new JasperaSentinel());
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent opponentElf = findPermanent(player2, "Jaspera Sentinel");
        assertThat(gqs.getEffectivePower(gd, opponentElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentElf, Keyword.DEATHTOUCH)).isFalse();

        harness.setHand(player1, List.of(new JasperaSentinel()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laterElf = findPermanent(player1, "Jaspera Sentinel");
        assertThat(gqs.getEffectivePower(gd, laterElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, laterElf)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, laterElf, Keyword.DEATHTOUCH)).isFalse();
        Permanent token = findPermanent(player1, "Elf Warrior");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void activatedAbilityCanBeActivatedRepeatedlyAndBoostsStack() {
        harness.addToBattlefield(player1, new ElvishWarmaster());
        harness.addMana(player1, ManaColor.GREEN, 14);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warmaster = findPermanent(player1, "Elvish Warmaster");
        assertThat(gqs.getEffectivePower(gd, warmaster)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, warmaster)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, warmaster, Keyword.DEATHTOUCH)).isTrue();
    }
}
