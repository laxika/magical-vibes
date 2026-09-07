package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.m.MasterOfPearls;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevealingWind.class, MasterOfPearls.class})
class RevealingWindTest extends BaseCardTest {

    @Test
    void preventsCombatDamageAndRevealsFaceDownCombatCreatures() {
        Permanent attackingCreature = addFaceDownCreature(player2);
        attackingCreature.setAttacking(true);
        Permanent blockingCreature = addFaceDownCreature(player1);
        blockingCreature.setBlocking(true);
        addFaceDownCreature(player2);
        Permanent faceUpAttacker = harness.addToBattlefieldAndReturn(player2, new MasterOfPearls());
        faceUpAttacker.setAttacking(true);
        harness.clearMessages();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player1, List.of(new RevealingWind()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.preventAllCombatDamage).isTrue();
        assertThat(harness.getConn1().getMessagesContaining("REVEAL_PERMANENT")).hasSize(2);
        assertThat(harness.getConn2().getMessagesContaining("REVEAL_PERMANENT")).isEmpty();
    }

    private Permanent addFaceDownCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new MasterOfPearls());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
