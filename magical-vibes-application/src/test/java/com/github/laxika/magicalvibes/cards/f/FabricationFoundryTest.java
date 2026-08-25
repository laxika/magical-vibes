package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FabricationFoundry.class, ChromaticStar.class, GrizzlyBears.class,
        MindStone.class, Ornithopter.class, PropheticPrism.class})
class FabricationFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("The mana ability produces mana restricted to artifacts")
    void manaAbilityRestrictsManaToArtifacts() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getArtifactOnlyMana(ManaColor.WHITE)).isEqualTo(1);

        harness.setHand(player1, List.of(new ChromaticStar()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The restricted mana cannot pay for a nonartifact spell")
    void restrictedManaCannotPayForNonartifact() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exiling a non-empty artifact subset sets X and returns an artifact")
    void exilesSelectedArtifactsAndReturnsTarget() {
        harness.addToBattlefield(player1, new FabricationFoundry());
        Permanent mindStone = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent ornithopter = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Card target = new PropheticPrism();
        harness.setGraveyard(player1, List.of(target));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("At least one artifact");

        harness.handleMultiplePermanentsChosen(player1, List.of(mindStone.getId(), ornithopter.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(mindStone.getCard().getId(), ornithopter.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()))).isTrue();
    }
}
