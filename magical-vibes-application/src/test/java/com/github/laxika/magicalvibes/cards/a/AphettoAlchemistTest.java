package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SlateOfAncestry;
import com.github.laxika.magicalvibes.cards.t.TribalGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AphettoAlchemist.class, Forest.class, GlorySeeker.class, Pacifism.class,
        SlateOfAncestry.class, TribalGolem.class})
class AphettoAlchemistTest extends BaseCardTest {

    @Test
    void untapsTargetCreature() {
        addReadyAlchemist(player1);
        Permanent target = addCreatureReady(player2, new GlorySeeker());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void untapsTargetArtifact() {
        addReadyAlchemist(player1);
        Permanent target = addReadyArtifact(player2);
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void untapsTargetArtifactCreature() {
        addReadyAlchemist(player1);
        Permanent target = addCreatureReady(player2, new TribalGolem());
        target.tap();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void cannotTargetAnEnchantment() {
        Permanent alchemist = addReadyAlchemist(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
        assertThat(alchemist.isTapped()).isFalse();
    }

    @Test
    void cannotTargetALand() {
        Permanent alchemist = addReadyAlchemist(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
        assertThat(alchemist.isTapped()).isFalse();
    }

    @Test
    void canUntapItself() {
        Permanent alchemist = addReadyAlchemist(player1);

        harness.activateAbility(player1, 0, null, alchemist.getId());
        assertThat(alchemist.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(alchemist.isTapped()).isFalse();
    }

    @Test
    void canBeMorphedFaceDownAndTurnedFaceUp() {
        harness.setHand(player1, List.of(new AphettoAlchemist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent alchemist = findPermanent(player1, "Aphetto Alchemist");
        assertThat(alchemist.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(alchemist));
        harness.passBothPriorities();

        assertThat(alchemist.isFaceDown()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    private Permanent addReadyAlchemist(Player player) {
        return addCreatureReady(player, new AphettoAlchemist());
    }

    private Permanent addReadyArtifact(Player player) {
        return harness.addToBattlefieldAndReturn(player, new SlateOfAncestry());
    }
}
