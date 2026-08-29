package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InfectiousCurse;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.SearingSpear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccursedWitchTest extends BaseCardTest {

    @Test
    void diesAndReturnsTransformedAttachedToTargetOpponent() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new AccursedWitch());
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, witch.getId());
        harness.passBothPriorities();

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(witch.getOriginalCard().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCard()).isInstanceOf(InfectiousCurse.class);
        assertThat(returned.isTransformed()).isTrue();
        assertThat(returned.getAttachedTo()).isEqualTo(player2.getId());
    }

    @Test
    void frontFaceReducesOpponentSpellTargetingIt() {
        Permanent witch = harness.addToBattlefieldAndReturn(player1, new AccursedWitch());
        harness.setHand(player2, List.of(new SearingSpear()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, witch.getId());

        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    void backFaceReducesControllerSpellTargetingEnchantedPlayer() {
        Permanent curse = new Permanent(new AccursedWitch());
        curse.setCard(curse.getOriginalCard().getBackFaceCard());
        curse.setTransformed(true);
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    void enchantedPlayerLosesLifeAndControllerGainsLifeAtUpkeep() {
        Permanent curse = new Permanent(new AccursedWitch());
        curse.setCard(curse.getOriginalCard().getBackFaceCard());
        curse.setTransformed(true);
        curse.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(curse);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }
}
