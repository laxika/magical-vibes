package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BearUmbra;
import com.github.laxika.magicalvibes.cards.d.DawnglareInvoker;
import com.github.laxika.magicalvibes.cards.d.DeathlessAngel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PawnOfUlamog;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AllIsDust.class, Forest.class, GrizzlyBears.class, Island.class, Mountain.class,
        Ornithopter.class, BearUmbra.class, DawnglareInvoker.class, DeathlessAngel.class,
        PawnOfUlamog.class, PropheticPrism.class})
class AllIsDustTest extends BaseCardTest {

    @Test
    @DisplayName("Each player sacrifices every colored permanent but keeps colorless permanents")
    void sacrificesColoredPermanents() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Island");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void sacrificesIndestructibleCreatureAndColoredAuraButKeepsColorlessArtifact() {
        var angel = harness.addToBattlefieldAndReturn(player1, new DeathlessAngel());
        var aura = harness.addToBattlefieldAndReturn(player1, new BearUmbra());
        aura.setAttachedTo(angel.getId());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 0, null, angel.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Deathless Angel");
        harness.assertInGraveyard(player1, "Bear Umbra");
        harness.assertNotOnBattlefield(player1, "Deathless Angel");
        harness.assertNotOnBattlefield(player1, "Bear Umbra");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
    }

    @Test
    void simultaneousSacrificesTriggerPawnForItselfAndAnotherCreature() {
        harness.addToBattlefield(player1, new PawnOfUlamog());
        harness.addToBattlefield(player1, new DawnglareInvoker());
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);
        for (int i = 0; i < 10; i++) {
            if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
                harness.handleMayAbilityChosen(player1, true);
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                break;
            }
        }

        harness.assertInGraveyard(player1, "Pawn of Ulamog");
        harness.assertInGraveyard(player1, "Dawnglare Invoker");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    void resolvesWhenNeitherPlayerControlsColoredPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new PropheticPrism());
        harness.setHand(player1, List.of(new AllIsDust()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player2, "Prophetic Prism");
        harness.assertInGraveyard(player1, "All Is Dust");
        assertThat(gd.stack).isEmpty();
    }
}
