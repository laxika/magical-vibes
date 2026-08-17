package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ToTheSlaughterTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, the target player chooses a creature or planeswalker")
    void withoutDeliriumTargetPlayerChoosesOnePermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNotNull();
        harness.handleMultiplePermanentsChosen(player2, List.of(planeswalker.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jace Beleren");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    @DisplayName("With delirium, sacrifices a creature and a planeswalker")
    void withDeliriumSacrificesBothTypes() {
        setDelirium();
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jace Beleren");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With delirium, a two-permanent choice must include both types")
    void withDeliriumRequiresBothTypesWhenPossible() {
        setDelirium();
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent planeswalker = addReadyJace(player2);

        castToTheSlaughter(player2.getId());

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(
                player2, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature and a planeswalker");

        harness.handleMultiplePermanentsChosen(player2, List.of(firstCreature.getId(), planeswalker.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Jace Beleren");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(secondCreature);
    }

    @Test
    @DisplayName("To the Slaughter can target only a player")
    void targetMustBePlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castToTheSlaughter(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new ToTheSlaughter()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void setDelirium() {
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
    }

    private Permanent addReadyJace(Player player) {
        Permanent jace = new Permanent(new JaceBeleren());
        jace.setCounterCount(CounterType.LOYALTY, 3);
        jace.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(jace);
        return jace;
    }
}
