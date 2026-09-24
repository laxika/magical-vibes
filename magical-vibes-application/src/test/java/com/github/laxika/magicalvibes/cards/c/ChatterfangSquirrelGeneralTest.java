package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChatterfangSquirrelGeneral.class, RaiseTheAlarm.class, GrizzlyBears.class, Forest.class})
class ChatterfangSquirrelGeneralTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Squirrel for each token created under its controller's control")
    void addsSquirrelsForEachCreatedToken() {
        harness.addToBattlefield(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2);
        assertThat(findPermanents(player1, "Squirrel")).hasSize(2);
    }

    @Test
    @DisplayName("Sacrifices X Squirrels to give a creature +X/-X")
    void sacrificesSquirrelsForPowerAndToughnessChange() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, indexOf(chatterfang), 0, 2, target.getId());
        harness.handlePermanentChosen(player1, squirrels.get(0).getId());
        harness.handlePermanentChosen(player1, squirrels.get(1).getId());

        assertThat(findPermanents(player1, "Squirrel")).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Requires a creature target")
    void rejectsNonCreatureTarget() {
        Permanent chatterfang = harness.addToBattlefieldAndReturn(player1, new ChatterfangSquirrelGeneral());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(chatterfang), 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
