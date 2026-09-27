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

import java.util.ArrayList;
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
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

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
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

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
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapSpirit, expensiveSpirit, nonSpirit)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentSpirit)));

        killKamiWithKagemaro();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    private void killKamiWithKagemaro() {
        addCreatureReady(player1, new KagemaroFirstToSuffer());
        harness.setHand(player1, List.of(new KagemaroFirstToSuffer()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
    }
}
