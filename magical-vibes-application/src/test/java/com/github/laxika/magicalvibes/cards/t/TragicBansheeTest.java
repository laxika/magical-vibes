package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TragicBansheeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -1/-1 without morbid")
    void givesMinusOneMinusOneWithoutMorbid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castTragicBanshee(targetId);
        resolveTragicBanshee();

        Permanent target = findPermanent(player2, targetId);
        assertThat(target.getPowerModifier()).isEqualTo(-1);
        assertThat(target.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("ETB gives an opponent's creature -13/-13 with morbid")
    void givesMinusThirteenMinusThirteenWithMorbid() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        castTragicBanshee(targetId);
        resolveTragicBanshee();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Morbid is checked when the ETB resolves")
    void morbidCheckedAtResolution() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        castTragicBanshee(targetId);
        harness.passBothPriorities();
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target your own creature")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new TragicBanshee()));
        addManaForTragicBanshee();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTragicBanshee(UUID targetId) {
        harness.setHand(player1, List.of(new TragicBanshee()));
        addManaForTragicBanshee();
        harness.castCreature(player1, 0, 0, targetId);
    }

    private void resolveTragicBanshee() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addManaForTragicBanshee() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player, UUID id) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getId().equals(id))
                .findFirst()
                .orElseThrow();
    }
}
