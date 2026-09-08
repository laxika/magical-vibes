package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IcefallTest extends BaseCardTest {

    @Test
    void destroysTargetArtifact() {
        Permanent target = new Permanent(new Ornithopter());
        gd.playerBattlefields.get(player2.getId()).add(target);
        giveIcefall();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    void destroysTargetLand() {
        Permanent target = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(target);
        giveIcefall();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    void cannotTargetCreatureThatIsNeitherArtifactNorLand() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);
        giveIcefall();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    @Test
    void recoverReturnsIcefallToHandWhenPaid() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(icefall);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(icefall);
    }

    @Test
    void recoverExilesIcefallWhenDeclined() {
        Card icefall = new Icefall();
        harness.setGraveyard(player1, List.of(icefall));
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(icefall);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(icefall);
    }

    private void giveIcefall() {
        harness.setHand(player1, List.of(new Icefall()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
