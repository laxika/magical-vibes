package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticConfluence.class, GrizzlyBears.class, Spellbook.class})
class MysticConfluenceTest extends BaseCardTest {

    @Test
    void repeatedDrawModeDrawsThreeCards() {
        harness.setHand(player1, List.of(new MysticConfluence()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 2, 2, 2);
        harness.castSorcery(player1, 0, modes);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void counterModeCountersTargetSpellAndDraws() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MysticConfluence()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana(player2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 0, 2, 2);
        harness.castInstant(player2, 0, modes, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void creatureModeReturnsTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MysticConfluence()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        harness.castSorcery(player1, 0, modes, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void creatureModeRejectsNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Spellbook());
        harness.setHand(player1, List.of(new MysticConfluence()));
        addMana(player1);

        int modes = ChooseOneEffect.encodeRepeatedModeSelection(3, 1, 2, 2);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, modes, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 5);
    }
}
