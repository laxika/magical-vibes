package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CatapultMaster.class, Forest.class})
class CatapultMasterTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping five Soldiers exiles target creature")
    void tapsFiveSoldiersAndExilesTargetCreature() {
        List<Permanent> soldiers = new ArrayList<>();
        soldiers.add(addCreatureReady(player1, new CatapultMaster()));
        for (int i = 0; i < 4; i++) {
            soldiers.add(addCreatureReady(player1, new CatapultMaster()));
        }
        Permanent target = addCreatureReady(player2, new CatapultMaster());

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
            addCreatureReady(player1, new CatapultMaster());
        }
        Permanent target = addCreatureReady(player2, new CatapultMaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped Soldier cannot be tapped again to pay the cost")
    void tappedSoldierDoesNotCount() {
        List<Permanent> soldiers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            soldiers.add(addCreatureReady(player1, new CatapultMaster()));
        }
        soldiers.get(1).tap();
        Permanent target = addCreatureReady(player2, new CatapultMaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(soldiers).extracting(Permanent::isTapped)
                .containsExactly(false, true, false, false, false);
    }

    @Test
    @DisplayName("Soldiers controlled by an opponent do not pay the cost")
    void opponentSoldiersDoNotCount() {
        List<Permanent> ownSoldiers = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            ownSoldiers.add(addCreatureReady(player1, new CatapultMaster()));
        }
        Permanent opponentSoldier = addCreatureReady(player2, new CatapultMaster());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentSoldier.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(ownSoldiers).allMatch(soldier -> !soldier.isTapped());
        assertThat(opponentSoldier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        addCreatureReady(player1, new CatapultMaster());
        for (int i = 0; i < 4; i++) {
            addCreatureReady(player1, new CatapultMaster());
        }
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target a creature you control")
    void canTargetOwnCreature() {
        List<Permanent> soldiers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            soldiers.add(addCreatureReady(player1, new CatapultMaster()));
        }
        Permanent target = soldiers.get(4);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.findExiledCard(target.getCard().getId())).isNotNull();
    }
}
