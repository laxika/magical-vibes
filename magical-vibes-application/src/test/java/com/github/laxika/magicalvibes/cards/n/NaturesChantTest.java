package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UniversalAutomaton;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NaturesChant.class, FountainOfYouth.class, AngelicChorus.class, GrizzlyBears.class, UniversalAutomaton.class})
class NaturesChantTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact")
    void destroysArtifact() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Fountain of Youth");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Fountain of Youth");
        harness.assertInGraveyard(player2, "Fountain of Youth");
    }

    @Test
    @DisplayName("Destroys target enchantment")
    void destroysEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        prepare();

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or enchantment");
    }

    @Test
    @DisplayName("Destroys an artifact creature")
    void destroysArtifactCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new UniversalAutomaton()).getId();
        prepare();

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Universal Automaton");
        harness.assertInGraveyard(player2, "Universal Automaton");
        harness.assertInGraveyard(player1, "Nature's Chant");
    }

    @Test
    @DisplayName("Can destroy your own artifact using white mana")
    void destroysOwnArtifactUsingWhiteMana() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new UniversalAutomaton()).getId();
        harness.setHand(player1, List.of(new NaturesChant()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player1, "Universal Automaton");
        harness.assertInGraveyard(player1, "Universal Automaton");
        harness.assertInGraveyard(player1, "Nature's Chant");
    }

    private void prepare() {
        harness.setHand(player1, List.of(new NaturesChant()));
        harness.addMana(player1, ManaColor.GREEN, 2);
    }
}
