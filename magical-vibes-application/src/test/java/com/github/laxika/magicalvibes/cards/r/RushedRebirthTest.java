package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RushedRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a lower-mana-value creature from the library onto the battlefield tapped when the target dies")
    void findsLowerManaValueCreatureWhenTargetDies() {
        harness.addToBattlefield(player2, new AirElemental());
        GrizzlyBears lowerCreature = new GrizzlyBears();
        AirElemental sameManaValueCreature = new AirElemental();
        harness.setLibrary(player1, List.of(lowerCreature, sameManaValueCreature));
        harness.setHand(player1, List.of(new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Air Elemental"));
        resolveStack();

        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        Permanent found = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == lowerCreature)
                .findFirst()
                .orElseThrow();
        assertThat(found.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sameManaValueCreature);
    }

    @Test
    @DisplayName("Does not find a creature with equal or greater mana value")
    void requiresStrictlyLowerManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        AirElemental sameManaValueCreature = new AirElemental();
        SerraAngel higherManaValueCreature = new SerraAngel();
        harness.setLibrary(player1, List.of(sameManaValueCreature, higherManaValueCreature));
        harness.setHand(player1, List.of(new RushedRebirth(), new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.castSorcery(player1, 0, player2.getId());
        resolveStack();

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(sameManaValueCreature, higherManaValueCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new RushedRebirth()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }
}
