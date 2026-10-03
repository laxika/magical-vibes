package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GhostLitRedeemer;
import com.github.laxika.magicalvibes.cards.k.KagemaroFirstToSuffer;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DeathknellKami.class, DeathmaskNezumi.class, GhostLitRedeemer.class,
        KagemaroFirstToSuffer.class})
class DeathknellKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Deathknell Kami +1/+1 until end of turn")
    void activationBoostsSelf() {
        Permanent kami = addCreatureReady(player1, new DeathknellKami());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices Deathknell Kami at the next end step")
    void activationSacrificesAtNextEndStep() {
        addCreatureReady(player1, new DeathknellKami());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deathknell Kami");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deathknell Kami");
        harness.assertInGraveyard(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Soulshift 1 returns a targeted Spirit with mana value 1 or less to hand")
    void soulshiftReturnsCheapSpiritToHand() {
        addCreatureReady(player1, new DeathknellKami());
        Card spirit = new GhostLitRedeemer();
        harness.setGraveyard(player1, List.of(spirit));

        killKamiWithKagemaro();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftMayBeDeclined() {
        addCreatureReady(player1, new DeathknellKami());
        Card spirit = new GhostLitRedeemer();
        harness.setGraveyard(player1, List.of(spirit));

        killKamiWithKagemaro();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift only offers its controller's Spirits with mana value 1 or less")
    void soulshiftRestrictsTargets() {
        addCreatureReady(player1, new DeathknellKami());
        Card cheapSpirit = new GhostLitRedeemer();
        Card expensiveSpirit = new KagemaroFirstToSuffer();
        Card nonSpirit = new DeathmaskNezumi();
        Card opponentSpirit = new GhostLitRedeemer();
        harness.setGraveyard(player1, List.of(cheapSpirit, expensiveSpirit, nonSpirit));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killKamiWithKagemaro();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    @Test
    @DisplayName("Repeated activations stack their boosts")
    void repeatedActivationsStack() {
        Permanent kami = addCreatureReady(player1, new DeathknellKami());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(3);
    }

    @Test
    @DisplayName("The delayed sacrifice triggers during the opponent's end step too")
    void sacrificesAtOpponentsEndStep() {
        addCreatureReady(player1, new DeathknellKami());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Deathknell Kami");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deathknell Kami");
        harness.assertInGraveyard(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Without an activation Deathknell Kami survives the end step")
    void noSacrificeWithoutActivation() {
        addCreatureReady(player1, new DeathknellKami());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Deathknell Kami");
        harness.assertNotInGraveyard(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Activation during an end step waits for the following end step and its boost expires")
    void endStepActivationWaitsUntilFollowingEndStep() {
        Permanent kami = addCreatureReady(player1, new DeathknellKami());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deathknell Kami");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.getEffectivePower(gd, kami)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Deathknell Kami");

        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Deathknell Kami");
        harness.assertNotOnBattlefield(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Soulshift does not return a target that leaves the graveyard before resolution")
    void soulshiftTargetLeavesGraveyard() {
        addCreatureReady(player1, new DeathknellKami());
        Card spirit = new GhostLitRedeemer();
        harness.setGraveyard(player1, List.of(spirit));
        killKamiWithKagemaro();
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));

        gd.playerGraveyards.get(player1.getId()).removeIf(card -> card.getId().equals(spirit.getId()));
        harness.setExile(player1, List.of(spirit));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Ghost-Lit Redeemer");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spirit);
    }

    @Test
    @DisplayName("Soulshift has no return when the graveyard has no eligible Spirit")
    void soulshiftWithoutEligibleSpirit() {
        addCreatureReady(player1, new DeathknellKami());
        harness.setGraveyard(player1, List.of(new DeathmaskNezumi()));

        killKamiWithKagemaro();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Deathknell Kami");
        harness.assertInGraveyard(player1, "Deathmask Nezumi");
        harness.assertNotInHand(player1, "Deathknell Kami");
        harness.assertNotInHand(player1, "Deathmask Nezumi");
    }

    private void killKamiWithKagemaro() {
        addCreatureReady(player1, new KagemaroFirstToSuffer());
        harness.setHand(player1, List.of(new KagemaroFirstToSuffer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
    }
}
