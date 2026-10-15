package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.Decommission;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.cards.p.Panharmonicon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConsulateCrackdown.class, GrizzlyBears.class, Naturalize.class,
        Ornithopter.class, PropheticPrism.class, Decommission.class, Panharmonicon.class})
class ConsulateCrackdownTest extends BaseCardTest {

    private void castAndResolveCrackdown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ConsulateCrackdown()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles all artifacts controlled by opponents")
    void etbExilesOpponentsArtifactsOnly() {
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castAndResolveCrackdown();

        harness.assertOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Prophetic Prism"));
    }

    @Test
    @DisplayName("Exiled artifacts return when Consulate Crackdown leaves")
    void exiledArtifactsReturnWhenSourceLeaves() {
        harness.addToBattlefield(player2, new PropheticPrism());

        castAndResolveCrackdown();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        UUID crackdownId = harness.getPermanentId(player1, "Consulate Crackdown");
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, crackdownId);

        harness.assertOnBattlefield(player2, "Prophetic Prism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(card -> card.getName().equals("Prophetic Prism"));
    }

    @Test
    @DisplayName("No artifacts are exiled if Crackdown leaves before its enter trigger resolves")
    void sourceLeavesBeforeEnterTriggerResolves() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ConsulateCrackdown()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Decommission()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Consulate Crackdown"));
        harness.assertNotOnBattlefield(player1, "Consulate Crackdown");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Artifacts entering after the exile trigger resolves are unaffected")
    void laterArtifactsAreNotExiled() {
        castAndResolveCrackdown();
        harness.addToBattlefield(player2, new Ornithopter());

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Multiple artifact creatures return untapped immediately when Crackdown leaves")
    void multipleArtifactsReturnImmediately() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new Ornithopter());
        castAndResolveCrackdown();
        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);

        harness.setHand(player1, List.of(new Decommission()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Consulate Crackdown"));

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2)
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returning artifacts enter simultaneously and see each other's enter triggers")
    void returningPanharmoniconDoublesPrismTrigger() {
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addToBattlefield(player2, new Panharmonicon());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Ornithopter(), new Ornithopter(), new Ornithopter()));
        castAndResolveCrackdown();

        harness.setHand(player1, List.of(new Decommission()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player1, "Consulate Crackdown"));
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertOnBattlefield(player2, "Panharmonicon");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }
}
