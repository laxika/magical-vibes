package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeningBanshee.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class KeeningBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature -2/-2")
    void etbWeakensTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveKeeningBanshee();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB can target your own creature")
    void canTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveKeeningBanshee();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("ETB debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        UUID targetId = target.getId();

        castKeeningBanshee(targetId);
        resolveKeeningBanshee();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB destroys a 2/2 creature")
    void lethalDebuffDestroysTarget() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castKeeningBanshee(targetId);
        resolveKeeningBanshee();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB fizzles if the target leaves before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castKeeningBanshee(targetId);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can be cast with no creatures around")
    void castWithoutTarget() {
        harness.castFromHand(player1, new KeeningBanshee(), "{2}{B}{B}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Keening Banshee");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new KeeningBanshee()));
        addManaForKeeningBanshee();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castKeeningBanshee(UUID targetId) {
        harness.setHand(player1, List.of(new KeeningBanshee()));
        addManaForKeeningBanshee();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveKeeningBanshee() {
        resolveAllTriggers();
    }

    private void addManaForKeeningBanshee() {
        harness.addMana(player1, ManaColor.BLACK, 4);
    }

}
