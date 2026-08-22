package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BattlegroundGeist;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeathknellKamiTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability gives Deathknell Kami +1/+1 until end of turn")
    void activationBoostsSelf() {
        Permanent kami = addKami();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kami)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, kami)).isEqualTo(2);
    }

    @Test
    @DisplayName("Activating the ability sacrifices Deathknell Kami at the next end step")
    void activationSacrificesAtNextEndStep() {
        addKami();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deathknell Kami");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Deathknell Kami");
        harness.assertInGraveyard(player1, "Deathknell Kami");
    }

    @Test
    @DisplayName("Soulshift 1 returns a targeted Spirit with mana value 1 or less to hand")
    void soulshiftReturnsCheapSpiritToHand() {
        harness.addToBattlefield(player1, new DeathknellKami());
        Card spirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(spirit)));

        killKamiWithWrath();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(spirit.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(spirit.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(spirit.getId()));
    }

    @Test
    @DisplayName("Soulshift only offers its controller's Spirits with mana value 1 or less")
    void soulshiftRestrictsTargets() {
        harness.addToBattlefield(player1, new DeathknellKami());
        Card cheapSpirit = new LanternKami();
        Card expensiveSpirit = new BattlegroundGeist();
        Card nonSpirit = new GrizzlyBears();
        Card opponentSpirit = new LanternKami();
        harness.setGraveyard(player1, new ArrayList<>(List.of(cheapSpirit, expensiveSpirit, nonSpirit)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentSpirit)));

        killKamiWithWrath();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(cheapSpirit.getId());
        assertThat(choice.validCardIds()).doesNotContain(expensiveSpirit.getId(), nonSpirit.getId(), opponentSpirit.getId());
    }

    private Permanent addKami() {
        Permanent kami = new Permanent(new DeathknellKami());
        kami.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kami);
        return kami;
    }

    private void killKamiWithWrath() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
