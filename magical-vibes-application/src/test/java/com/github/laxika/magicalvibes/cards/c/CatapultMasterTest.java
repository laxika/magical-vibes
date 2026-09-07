package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcatianJavelineers;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultMaster.class, IcatianJavelineers.class, GrizzlyBears.class, Forest.class})
class CatapultMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five Soldiers exiles target creature")
    void tapsFiveSoldiersAndExilesTargetCreature() {
        List<Permanent> soldiers = new ArrayList<>();
        soldiers.add(addCreatureReady(player1, new CatapultMaster()));
        for (int i = 0; i < 4; i++) {
            soldiers.add(addCreatureReady(player1, new IcatianJavelineers()));
        }
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(soldiers).allSatisfy(soldier -> assertThat(soldier.isTapped()).isTrue());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card() == target.getCard());
    }

    @Test
    @DisplayName("Ability requires five untapped Soldiers")
    void requiresFiveUntappedSoldiers() {
        addCreatureReady(player1, new CatapultMaster());
        for (int i = 0; i < 3; i++) {
            addCreatureReady(player1, new IcatianJavelineers());
        }
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        UUID targetId = target.getId();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        addCreatureReady(player1, new CatapultMaster());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new IcatianJavelineers());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
