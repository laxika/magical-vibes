package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Innocent Blood")
@CardUsed({InnocentBlood.class, AngelicWall.class, DuskImp.class, Forest.class, Mountain.class})
class InnocentBloodTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices their only creature")
    void eachPlayerSacrificesTheirOnlyCreature() {
        harness.addToBattlefield(player1, new DuskImp());
        harness.addToBattlefield(player2, new AngelicWall());

        castInnocentBlood();

        harness.assertNotOnBattlefield(player1, "Dusk Imp");
        harness.assertNotOnBattlefield(player2, "Angelic Wall");
        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertInGraveyard(player2, "Angelic Wall");
    }

    @Test
    @DisplayName("Each player chooses which creature to sacrifice")
    void eachPlayerChoosesCreatureToSacrifice() {
        Permanent player1Imp = harness.addToBattlefieldAndReturn(player1, new DuskImp());
        harness.addToBattlefield(player1, new AngelicWall());
        harness.addToBattlefield(player2, new DuskImp());
        Permanent player2Wall = harness.addToBattlefieldAndReturn(player2, new AngelicWall());

        castInnocentBlood();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId())
                .isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(player1Imp.getId()));

        choice = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(player2Wall.getId()));

        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertOnBattlefield(player1, "Angelic Wall");
        harness.assertInGraveyard(player2, "Angelic Wall");
        harness.assertOnBattlefield(player2, "Dusk Imp");
    }

    @Test
    @DisplayName("A player without a creature is unaffected")
    void playerWithoutACreatureIsUnaffected() {
        harness.addToBattlefield(player1, new DuskImp());
        harness.addToBattlefield(player2, new Forest());

        castInnocentBlood();

        harness.assertInGraveyard(player1, "Dusk Imp");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
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
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new InnocentBlood(), "{B}");
        harness.passBothPriorities();
    }
}
