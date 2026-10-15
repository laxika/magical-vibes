package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TributeToTheWild.class, GloriousAnthem.class, Ornithopter.class, GrizzlyBears.class})
class TributeToTheWildTest extends BaseCardTest {

    @Test
    void eachOpponentSacrificesAnArtifactOrEnchantmentOfTheirChoice() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(artifact.getId(), enchantment.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(enchantment.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(artifact, creature);
        harness.assertInGraveyard(player2, "Glorious Anthem");
    }

    @Test
    void doesNothingWhenOpponentControlsNoArtifactOrEnchantment() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void opponentCanChooseArtifactInsteadOfEnchantment() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

        cast();

        harness.handleMultiplePermanentsChosen(player2, List.of(artifact.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(enchantment);
        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sacrificesSoleArtifactWithoutPrompting() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        harness.assertInGraveyard(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Ornithopter");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void sacrificesSoleEnchantmentWithoutPrompting() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        cast();

        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Glorious Anthem");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void cast() {
        harness.setHand(player1, List.of(new TributeToTheWild()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
