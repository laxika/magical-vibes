package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantGrace.class, RadiantRestraints.class, GrizzlyBears.class, SearingSpear.class})
class RadiantGraceTest extends BaseCardTest {

    @Test
    void frontFaceBoostsAndGrantsVigilanceToEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new RadiantGrace());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void enchantedCreatureDiesAndReturnsAsCurseAttachedToTargetOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new RadiantGrace());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(aura.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(RadiantRestraints.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void backFaceMakesOnlyEnchantedPlayersCreaturesEnterTapped() {
        Permanent curse = new Permanent(new RadiantGrace());
        curse.setCard(curse.getOriginalCard().getBackFaceCard());
        curse.setTransformed(true);
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent opponentCreature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        assertThat(opponentCreature.isTapped()).isTrue();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent controllerCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof GrizzlyBears)
                .findFirst()
                .orElseThrow();
        assertThat(controllerCreature.isTapped()).isFalse();
    }
}
