package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Charisma")
class CharismaTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of a creature damaged by the enchanted creature")
    void gainsControlOfDamagedCreature() {
        Permanent enchantedCreature = setUpCombat();
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new WallOfWood());
        damagedCreature.setSummoningSick(false);
        damagedCreature.setBlocking(true);
        damagedCreature.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(damagedCreature);
        assertThat(enchantedCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Control ends when Charisma leaves the battlefield")
    void controlEndsWhenCharismaLeavesBattlefield() {
        setUpCombat();
        Permanent damagedCreature = harness.addToBattlefieldAndReturn(player2, new WallOfWood());
        damagedCreature.setSummoningSick(false);
        damagedCreature.setBlocking(true);
        damagedCreature.addBlockingTarget(0);

        resolveCombat();

        harness.setHand(player1, java.util.List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent charisma = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof Charisma)
                .findFirst()
                .orElseThrow();
        harness.castInstant(player1, 0, charisma.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(damagedCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(damagedCreature);
    }

    private Permanent setUpCombat() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        enchantedCreature.setSummoningSick(false);
        enchantedCreature.setAttacking(true);

        Permanent charisma = new Permanent(new Charisma());
        charisma.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(charisma);
        return enchantedCreature;
    }
}
