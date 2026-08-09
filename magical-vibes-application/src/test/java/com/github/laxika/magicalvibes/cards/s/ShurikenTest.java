package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShurikenTest extends BaseCardTest {

    @Test
    @DisplayName("Shuriken deals 2 damage and its target's controller gains it")
    void damagesCreatureAndChangesControl() {
        Permanent creature = addCreatureReady(player1, new HillGiant());
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(creature.getId());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(shuriken);
    }

    @Test
    @DisplayName("Shuriken stays with its controller when unattached from a Ninja")
    void doesNotChangeControlWhenUnattachedFromNinja() {
        Permanent ninja = addCreatureReady(player1, new HillGiant());
        TestCards.mutableCard(ninja).setSubtypes(List.of(CardSubtype.NINJA));
        Permanent shuriken = addShurikenReady(player1);
        shuriken.setAttachedTo(ninja.getId());
        Permanent target = addCreatureReady(player2, new HillGiant());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(shuriken.getAttachedTo()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shuriken);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(shuriken);
    }

    private Permanent addShurikenReady(Player player) {
        Permanent permanent = new Permanent(new Shuriken());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
