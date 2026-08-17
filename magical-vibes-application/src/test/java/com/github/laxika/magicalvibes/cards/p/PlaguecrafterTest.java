package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Plaguecrafter")
class PlaguecrafterTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices a creature, with all choices resolving together")
    void eachPlayerSacrificesCreature() {
        GameData gd = harness.getGameData();
        Permanent opponentBears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(opponentBears);
        gd.playerBattlefields.get(player2.getId()).add(new Permanent(new GrizzlyBears()));

        castPlaguecrafter();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        harness.handleMultiplePermanentsChosen(player2, List.of(opponentBears.getId()));

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A player without a creature or planeswalker discards a card")
    void playerWithoutSacrificeTargetDiscards() {
        Shock discard = new Shock();
        harness.setHand(player1, List.of(new Plaguecrafter()));
        harness.setHand(player2, List.of(discard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Plaguecrafter");
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingEffectResolutionEntry).isNull();
    }

    @Test
    @DisplayName("A noncreature permanent does not satisfy the sacrifice requirement")
    void noncreaturePermanentDoesNotSatisfyRequirement() {
        Shock discard = new Shock();
        harness.setHand(player1, List.of(new Plaguecrafter()));
        harness.setHand(player2, List.of(discard));
        harness.addToBattlefield(player2, new ZuranOrb());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Zuran Orb");
        harness.assertInGraveyard(player2, "Shock");
    }

    private void castPlaguecrafter() {
        harness.setHand(player1, List.of(new Plaguecrafter()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
