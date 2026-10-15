package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CrimsonKobolds;
import com.github.laxika.magicalvibes.cards.k.KherKeep;
import com.github.laxika.magicalvibes.cards.k.KoboldsOfKherKeep;
import com.github.laxika.magicalvibes.cards.p.PsychicPaper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RohgahhOfKherKeep.class, KherKeep.class, BarbaryApes.class,
        KoboldsOfKherKeep.class, Boomerang.class, CrimsonKobolds.class, PsychicPaper.class})
class RohgahhOfKherKeepTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures named Kobolds of Kher Keep that you control")
    void boostsKoboldsYouControl() {
        Permanent kobold = createKoboldToken(player1);
        Permanent opponentKobold = createKoboldToken(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());

        harness.addToBattlefield(player1, new RohgahhOfKherKeep());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps control and does not tap the permanents")
    void payingUpkeepCostKeepsControl() {
        Permanent kobold = createKoboldToken(player1);
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        beginUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rohgahh, kobold);
        assertThat(rohgahh.isTapped()).isFalse();
        assertThat(kobold.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep cost taps and transfers Rohgahh and all matching creatures")
    void decliningUpkeepCostTransfersAllMatchingCreatures() {
        Permanent kobold = createKoboldToken(player1);
        Permanent opponentKobold = createKoboldToken(player2);
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BarbaryApes());

        beginUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(rohgahh.isTapped()).isTrue();
        assertThat(kobold.isTapped()).isTrue();
        assertThat(opponentKobold.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears).doesNotContain(rohgahh, kobold);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rohgahh, kobold, opponentKobold);
        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentKobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentKobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A differently named Kobold is neither boosted nor transferred")
    void leavesDifferentlyNamedKoboldsAlone() {
        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        Permanent crimson = harness.addToBattlefieldAndReturn(player1, new CrimsonKobolds());
        harness.addToBattlefield(player1, new RohgahhOfKherKeep());

        assertThat(gqs.getEffectivePower(gd, kobold)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, crimson)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, crimson)).isEqualTo(1);

        beginUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(crimson).doesNotContain(kobold);
        assertThat(crimson.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kobold);
        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Insufficient red mana cannot avoid the upkeep penalty")
    void unableToPayTransfersPermanents() {
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        beginUpkeep(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(rohgahh, kobold);
        assertThat(rohgahh.isTapped()).isTrue();
        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The new controller receives the next upkeep payment choice")
    void upkeepFollowsTheNewController() {
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        beginUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        beginUpkeep(player2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(rohgahh, kobold);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(rohgahh, kobold);
        assertThat(rohgahh.isTapped()).isTrue();
        assertThat(kobold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Removing Rohgahh in response does not stop the transfer of Kobolds")
    void transfersKoboldsEvenWhenSourceLeavesBeforeResolution() {
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        Permanent kobold = harness.addToBattlefieldAndReturn(player1, new KoboldsOfKherKeep());
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, rohgahh.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Rohgahh of Kher Keep");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kobold).doesNotContain(rohgahh);
        assertThat(kobold.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, kobold)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kobold)).isEqualTo(1);
    }

    @Test
    @DisplayName("Rohgahh receives its own bonus when named Kobolds of Kher Keep")
    void boostsItselfWhenItHasTheMatchingName() {
        Permanent paper = harness.addToBattlefieldAndReturn(player1, new PsychicPaper());
        Permanent rohgahh = harness.addToBattlefieldAndReturn(player1, new RohgahhOfKherKeep());
        harness.addToBattlefield(player1, new KoboldsOfKherKeep());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(paper), null, rohgahh.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Kobolds of Kher Keep");
        harness.handleListChoice(player1, "KOBOLD");

        assertThat(gqs.getEffectivePower(gd, rohgahh)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, rohgahh)).isEqualTo(7);
    }

    private void beginUpkeep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        gd.turnNumber = 2;
        advanceToUpkeep(activePlayer);
        resolveAllTriggers();
    }

    private Permanent createKoboldToken(com.github.laxika.magicalvibes.model.Player player) {
        Permanent kherKeep = harness.addToBattlefieldAndReturn(player, new KherKeep());
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.activateAbility(player,
                gd.playerBattlefields.get(player.getId()).indexOf(kherKeep), 1, null, null);
        harness.passBothPriorities();
        return findPermanent(player, "Kobolds of Kher Keep");
    }
}
