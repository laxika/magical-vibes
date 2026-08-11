package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DredgeTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature lets Dredge draw a card")
    void sacrificesCreatureAndDrawsCard() {
        Permanent creature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(creature);

        harness.setHand(player1, List.of(new Dredge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Sacrificing a land lets Dredge draw a card")
    void sacrificesLandAndDrawsCard() {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(land);

        harness.setHand(player1, List.of(new Dredge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithSacrifice(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Cannot sacrifice a permanent that is neither a creature nor a land")
    void cannotSacrificeNonCreatureNonLandPermanent() {
        Permanent artifact = new Permanent(new Spellbook());
        gd.playerBattlefields.get(player1.getId()).add(artifact);

        harness.setHand(player1, List.of(new Dredge()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstantWithSacrifice(player1, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or land");
    }
}
