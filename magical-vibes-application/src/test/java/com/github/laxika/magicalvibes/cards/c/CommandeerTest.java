package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommandeerTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a noncreature permanent spell")
    void gainsControlOfNoncreaturePermanentSpell() {
        LeoninScimitar scimitar = new LeoninScimitar();
        harness.setHand(player1, List.of(scimitar));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        harness.castArtifact(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, scimitar.getId(), List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player1, "Leonin Scimitar");
        assertThat(harness.getGameData().exiledCards)
                .extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Counterspell", "Boomerang");
    }

    @Test
    @DisplayName("Can choose new targets for the spell it controls")
    void canChooseNewTargets() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithAlternateExileFromHand(player2, 0, lavaAxe.getId(), List.of(1, 2));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
        harness.assertInGraveyard(player1, "Lava Axe");
        harness.assertInGraveyard(player2, "Commandeer");
    }

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Commandeer(), new Counterspell(), new Boomerang()));
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithAlternateExileFromHand(
                player2, 0, bears.getId(), List.of(1, 2)))
                .isInstanceOf(IllegalStateException.class);
    }
}
