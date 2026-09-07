package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Disintegrate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Fork.class, Disintegrate.class, GrizzlyBears.class, Unsummon.class})
class ForkTest extends BaseCardTest {

    @Test
    void copiesAsRedAndOffersNewTargets() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Unsummon unsummon = new Unsummon();

        harness.setHand(player1, List.of(unsummon));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, unsummon.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        StackEntry copy = gameData.stack.getLast();
        assertThat(copy.isCopy()).isTrue();
        assertThat(copy.getCard().getColors()).containsExactly(CardColor.RED);
        assertThat(copy.getTargetId()).isEqualTo(firstTarget.getId());

        harness.handleMayAbilityChosen(player2, true);

        assertThat(gameData.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gameData.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstTarget.getId(), secondTarget.getId());

        harness.handlePermanentChosen(player2, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .contains(firstTarget)
                .doesNotContain(secondTarget);
        assertThat(gameData.playerHands.get(player1.getId()))
                .contains(secondTarget.getCard());

        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .doesNotContain(firstTarget, secondTarget);
        assertThat(gameData.playerHands.get(player1.getId()))
                .contains(firstTarget.getCard(), secondTarget.getCard());
    }

    @Test
    void copiesSorceryAndPreservesItsEffect() {
        Disintegrate disintegrate = new Disintegrate();
        harness.setHand(player1, List.of(disintegrate));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, disintegrate.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetCreatureSpell() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0,
                harness.getGameData().stack.getFirst().getCard().getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
