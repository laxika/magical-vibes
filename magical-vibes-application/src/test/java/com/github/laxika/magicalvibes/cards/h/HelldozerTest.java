package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GhostQuarter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Helldozer.class, Forest.class, GhostQuarter.class, GrizzlyBears.class})
class HelldozerTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonbasic land and untaps itself")
    void destroysNonbasicLandAndUntapsItself() {
        Permanent helldozer = addReadyHelldozer();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GhostQuarter());

        activateAgainst(helldozer, land);

        assertThat(helldozer.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player2, "Ghost Quarter");
        harness.assertInGraveyard(player2, "Ghost Quarter");
    }

    @Test
    @DisplayName("Destroys a basic land without untapping itself")
    void destroysBasicLandWithoutUntappingItself() {
        Permanent helldozer = addReadyHelldozer();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        activateAgainst(helldozer, land);

        assertThat(helldozer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent helldozer = addReadyHelldozer();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(helldozer), null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyHelldozer() {
        Permanent helldozer = harness.addToBattlefieldAndReturn(player1, new Helldozer());
        helldozer.setSummoningSick(false);
        return helldozer;
    }

    private void activateAgainst(Permanent helldozer, Permanent land) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.activateAbility(player1, indexOf(helldozer), null, land.getId());
        harness.passBothPriorities();
    }

    private int indexOf(Permanent helldozer) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(helldozer);
    }
}
