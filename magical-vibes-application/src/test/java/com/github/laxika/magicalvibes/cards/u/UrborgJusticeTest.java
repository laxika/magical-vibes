package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BenalishKnight;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrborgJustice.class, BenalishKnight.class, MindStone.class, RedwoodTreefolk.class})
class UrborgJusticeTest extends BaseCardTest {

    private void castUrborgJustice() {
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Opponent sacrifices one creature per creature that died under the caster's control")
    void sacrificesOnePerControllerDeath() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 2, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Redwood Treefolk");
        harness.assertInGraveyard(player2, "Benalish Knight");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which creatures to sacrifice when they control more than died")
    void opponentChoosesWhenMoreCreaturesThanDeaths() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new BenalishKnight());

        castUrborgJustice();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        harness.handleMultiplePermanentsChosen(player2,
                List.of(harness.getPermanentId(player2, "Benalish Knight")));

        harness.assertInGraveyard(player2, "Benalish Knight");
        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Nothing is sacrificed when no creature died under the caster's control")
    void noSacrificeWithoutDeaths() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player2.getId(), 3, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());

        castUrborgJustice();

        harness.assertOnBattlefield(player2, "Redwood Treefolk");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Urborg Justice");
    }

    @Test
    @DisplayName("Only sacrifices creatures, not other permanents")
    void doesNotSacrificeNoncreaturePermanents() {
        gd.creaturesPutIntoOwnGraveyardThisTurnCount.merge(player1.getId(), 1, Integer::sum);
        harness.addToBattlefield(player2, new RedwoodTreefolk());
        harness.addToBattlefield(player2, new MindStone());

        castUrborgJustice();

        harness.assertInGraveyard(player2, "Redwood Treefolk");
        harness.assertOnBattlefield(player2, "Mind Stone");
    }

    @Test
    @DisplayName("Cannot target the spell's controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new UrborgJustice()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
