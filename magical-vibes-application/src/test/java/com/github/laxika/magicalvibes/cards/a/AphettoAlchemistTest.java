package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AphettoAlchemist.class, AngelsFeather.class, GrizzlyBears.class, Pacifism.class})
class AphettoAlchemistTest extends BaseCardTest {

    @Test
    void untapsTargetCreature() {
        addReadyAlchemist(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
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
    void cannotTargetAnEnchantment() {
        addReadyAlchemist(player1);
        Permanent target = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(target);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
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
    }

    private Permanent addReadyAlchemist(Player player) {
        return addCreatureReady(player, new AphettoAlchemist());
    }

    private Permanent addReadyArtifact(Player player) {
        Permanent permanent = new Permanent(new AngelsFeather());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
