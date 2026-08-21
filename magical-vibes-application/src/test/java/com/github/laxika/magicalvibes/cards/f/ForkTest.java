package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({Fork.class, Boomerang.class, GrizzlyBears.class})
class ForkTest extends BaseCardTest {

    @Test
    void copiesAsRedAndOffersNewTargets() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Boomerang boomerang = new Boomerang();

        harness.setHand(player1, List.of(boomerang));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player1, 0, firstTarget.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, boomerang.getId());
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
                .contains(secondTarget.getId());
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
