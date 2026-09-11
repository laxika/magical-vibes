package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Flameshape;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SageOfFables;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GandalfGoblinsBaneFlameshape.class, Flameshape.class, Island.class, SageOfFables.class,
        Shock.class})
class GandalfGoblinsBaneFlameshapeTest extends BaseCardTest {

    @Test
    void noncreatureSpellBoostsGandalfAndDamagesEachOpponent() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void creatureSpellDoesNotTriggerGandalf() {
        Permanent gandalf = harness.addToBattlefieldAndReturn(player1,
                new GandalfGoblinsBaneFlameshape());
        harness.setHand(player1, List.of(new SageOfFables()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gandalf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gandalf)).isEqualTo(3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void flameshapeExilesTopTwoFaceDownAndRequiresWizardToPlayThem() {
        Card first = new Shock();
        Card second = new Island();
        GandalfGoblinsBaneFlameshape card = new GandalfGoblinsBaneFlameshape();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(first.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(second.getId()).faceDown()).isTrue();
        assertThatThrownBy(() -> harness.castFromExile(player1, first.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player1, new SageOfFables());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castFromExile(player1, first.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }
}
