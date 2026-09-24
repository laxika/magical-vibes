package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.cards.k.KamiOfTheHonoredDead;
import com.github.laxika.magicalvibes.cards.t.TorrentOfStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarbingerOfSpring.class, FrostOgre.class, KamiOfFalseHope.class,
        KamiOfTheHonoredDead.class, TorrentOfStone.class})
class HarbingerOfSpringTest extends BaseCardTest {

    private void killHarbinger() {
        harness.setHand(player1, List.of(new TorrentOfStone()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Harbinger of Spring"));
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Protection from non-Spirit creatures prevents a non-Spirit creature from blocking")
    void nonSpiritCreatureCannotBlock() {
        addCreatureReady(player1, new HarbingerOfSpring());
        addCreatureReady(player2, new FrostOgre());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A Spirit creature can block Harbinger of Spring")
    void spiritCreatureCanBlock() {
        addCreatureReady(player1, new HarbingerOfSpring());
        Permanent blocker = addCreatureReady(player2, new KamiOfFalseHope());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Soulshift returns a targeted Spirit with mana value 4 or less from your graveyard to your hand")
    void soulshiftReturnsEligibleSpirit() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        Card eligible = new KamiOfFalseHope();
        Card nonSpirit = new FrostOgre();
        Card tooExpensive = new KamiOfTheHonoredDead();
        Card opponentSpirit = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(eligible, nonSpirit, tooExpensive));
        harness.setGraveyard(player2, List.of(opponentSpirit));

        killHarbinger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kami of False Hope");
        harness.assertInGraveyard(player1, "Frost Ogre");
        harness.assertInGraveyard(player1, "Kami of the Honored Dead");
        harness.assertInGraveyard(player2, "Kami of False Hope");
    }

    @Test
    @DisplayName("Soulshift may be declined")
    void soulshiftCanBeDeclined() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        Card eligible = new KamiOfFalseHope();
        harness.setGraveyard(player1, List.of(eligible));

        killHarbinger();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kami of False Hope");
        harness.assertNotInHand(player1, "Kami of False Hope");
    }

    @Test
    @DisplayName("Soulshift does not present a choice without a legal target")
    void noEligibleTargetMeansNoChoice() {
        harness.addToBattlefield(player1, new HarbingerOfSpring());
        harness.setGraveyard(player1, List.of(new FrostOgre(), new KamiOfTheHonoredDead()));

        killHarbinger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }
}
