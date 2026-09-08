package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HadaFreeblade;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwariShapeshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Can enter as a copy of an Ally creature")
    void copiesAllyCreature() {
        harness.addToBattlefield(player2, new HadaFreeblade());
        harness.setHand(player1, List.of(new JwariShapeshifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        UUID allyId = harness.getPermanentId(player2, "Hada Freeblade");
        harness.handlePermanentChosen(player1, allyId);

        Permanent shapeshifter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Jwari Shapeshifter"))
                .findFirst()
                .orElse(null);

        assertThat(shapeshifter).isNotNull();
        assertThat(shapeshifter.getCard().getName()).isEqualTo("Hada Freeblade");
        assertThat(shapeshifter.getCard().getPower()).isEqualTo(0);
        assertThat(shapeshifter.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not copy a non-Ally creature")
    void doesNotCopyNonAllyCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JwariShapeshifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getName().equals("Jwari Shapeshifter"));
        harness.assertInGraveyard(player1, "Jwari Shapeshifter");
    }
}
