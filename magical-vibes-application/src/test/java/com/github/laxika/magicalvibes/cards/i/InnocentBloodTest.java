package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Innocent Blood")
class InnocentBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices their only creature")
    void eachPlayerSacrificesTheirOnlyCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        castInnocentBlood();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Each player chooses which creature to sacrifice")
    void eachPlayerChoosesCreatureToSacrifice() {
        Permanent player1Bears = new Permanent(new GrizzlyBears());
        Permanent player1Giant = new Permanent(new GiantSpider());
        Permanent player2Bears = new Permanent(new GrizzlyBears());
        Permanent player2Giant = new Permanent(new GiantSpider());
        harness.getGameData().playerBattlefields.get(player1.getId()).add(player1Bears);
        harness.getGameData().playerBattlefields.get(player1.getId()).add(player1Giant);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(player2Bears);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(player2Giant);

        castInnocentBlood();

        GameData gd = harness.getGameData();
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId())
                .isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Bears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(player2Giant.getId()));

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Noncreature permanents are not sacrificed")
    void noncreaturePermanentsAreNotSacrificed() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        castInnocentBlood();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    private void castInnocentBlood() {
        harness.setHand(player1, List.of(new InnocentBlood()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
