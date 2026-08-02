package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HornetNestTest extends BaseCardTest {

    @Test
    @DisplayName("Shock dealing 2 damage to Hornet Nest creates two 1/1 flying deathtouch Insect tokens")
    void spellDamageCreatesThatManyTokens() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID nestId = harness.getPermanentId(player2, "Hornet Nest");
        harness.castInstant(player1, 0, nestId);
        harness.passBothPriorities(); // Resolve Shock — 2 damage, lethal for the 0/2 Nest
        harness.passBothPriorities(); // Resolve the ON_DEALT_DAMAGE trigger

        harness.assertInGraveyard(player2, "Hornet Nest");

        List<Permanent> tokens = findPermanents(player2, "Insect");
        assertThat(tokens).hasSize(2);
        assertThat(tokens.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(tokens.getFirst().getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getKeywords())
                .contains(Keyword.FLYING, Keyword.DEATHTOUCH);
        assertThat(tokens.getFirst().getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Blocking a 2/2 attacker creates two Insect tokens")
    void combatDamageCreatesThatManyTokens() {
        harness.addToBattlefield(player2, new HornetNest());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent nest = gd.playerBattlefields.get(player2.getId()).getFirst();
        nest.setSummoningSick(false);
        nest.setBlocking(true);
        nest.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Insect")).hasSize(2);
        harness.assertInGraveyard(player2, "Hornet Nest");
    }
}
