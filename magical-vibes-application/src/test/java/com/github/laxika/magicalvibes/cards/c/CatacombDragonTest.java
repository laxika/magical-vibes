package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CatacombDragonTest extends BaseCardTest {

    @Test
    @DisplayName("A 2/2 blocker gets -1/-0 (half its power, rounded down)")
    void shrinksBlockerByHalfItsPower() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new GrizzlyBears());

        block();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An odd power is rounded down: a 3/3 blocker gets -1/-0")
    void roundsDown() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new HillGiant());

        block();

        assertThat(blocker.getPowerModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("An 8/8 blocker gets -4/-0")
    void shrinksLargeBlocker() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new AvatarOfMight());

        block();

        assertThat(blocker.getPowerModifier()).isEqualTo(-4);
    }

    @Test
    @DisplayName("An artifact creature blocker is unaffected")
    void artifactBlockerUnaffected() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new ClayStatue());

        block();

        assertThat(blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("A Dragon blocker is unaffected")
    void dragonBlockerUnaffected() {
        addAttackingDragon();
        Permanent blocker = addFlyingBlocker(new AvatarOfMight());
        blocker.getGrantedSubtypes().add(CardSubtype.DRAGON);

        block();

        assertThat(blocker.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("With two blockers only the qualifying one shrinks")
    void onlyQualifyingBlockerShrinks() {
        addAttackingDragon();
        Permanent bears = addFlyingBlocker(new GrizzlyBears());
        Permanent dragonBlocker = addFlyingBlocker(new HillGiant());
        dragonBlocker.getGrantedSubtypes().add(CardSubtype.DRAGON);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(-1);
        assertThat(dragonBlocker.getPowerModifier()).isZero();
    }

    private void addAttackingDragon() {
        Permanent dragon = new Permanent(new CatacombDragon());
        dragon.setSummoningSick(false);
        dragon.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(dragon);
    }

    private Permanent addFlyingBlocker(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.getGrantedKeywords().add(Keyword.FLYING);
        gd.playerBattlefields.get(player2.getId()).add(permanent);
        return permanent;
    }

    private void block() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
    }
}
