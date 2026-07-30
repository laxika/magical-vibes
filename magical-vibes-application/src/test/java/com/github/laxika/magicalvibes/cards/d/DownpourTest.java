package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DownpourTest extends BaseCardTest {

    @Test
    @DisplayName("Taps three target creatures")
    void tapsThreeTargetCreatures() {
        Permanent c1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent c2 = addReadyCreature(player2, new GiantSpider());
        Permanent c3 = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(c1.getId(), c2.getId(), c3.getId()));
        harness.passBothPriorities();

        assertThat(c1.isTapped()).isTrue();
        assertThat(c2.isTapped()).isTrue();
        assertThat(c3.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target just one creature")
    void canTargetJustOne() {
        Permanent creature = addReadyCreature(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Downpour");
    }

    @Test
    @DisplayName("Cannot target more than three creatures")
    void cannotTargetMoreThanThree() {
        Permanent c1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent c2 = addReadyCreature(player2, new GiantSpider());
        Permanent c3 = addReadyCreature(player2, new GrizzlyBears());
        Permanent c4 = addReadyCreature(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(c1.getId(), c2.getId(), c3.getId(), c4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        addReadyCreature(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(fountainId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Skips targets that left the battlefield before resolution")
    void skipsRemovedTargets() {
        Permanent c1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent c2 = addReadyCreature(player2, new GiantSpider());

        harness.setHand(player1, List.of(new Downpour()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(c1.getId(), c2.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(c1);
        harness.passBothPriorities();

        assertThat(c2.isTapped()).isTrue();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
