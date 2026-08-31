package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GreaterAuramancy;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EarlyWinter.class, FountainOfYouth.class, GreaterAuramancy.class, GrizzlyBears.class})
class EarlyWinterTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode exiles the targeted creature")
    void exilesTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castEarlyWinter(0, creature.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(creature.getCard());
    }

    @Test
    @DisplayName("Creature mode cannot target an artifact")
    void creatureModeRejectsArtifactTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new EarlyWinter()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchantment mode exiles one enchantment and leaves other permanent types alone")
    void opponentExilesAnEnchantment() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castEarlyWinter(1, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(enchantment.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchantment.getCard());
    }

    @Test
    @DisplayName("Opponent chooses which enchantment to exile when they control several")
    void opponentChoosesAnEnchantment() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GreaterAuramancy());
        castEarlyWinter(1, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileChosenPermanent.class);

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(first.getId()))
                .anyMatch(permanent -> permanent.getId().equals(second.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(first.getCard());
    }

    @Test
    @DisplayName("Enchantment mode does nothing when the opponent controls no enchantments")
    void opponentWithNoEnchantmentsExilesNothing() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        castEarlyWinter(1, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(artifact.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    private void castEarlyWinter(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new EarlyWinter()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }
}
