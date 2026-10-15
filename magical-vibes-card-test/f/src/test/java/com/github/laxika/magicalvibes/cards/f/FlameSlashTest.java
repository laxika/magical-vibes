package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.u.UlamogsCrusher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameSlash.class, GrizzlyBears.class, LeoninScimitar.class, UlamogsCrusher.class})
class FlameSlashTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent target = findPermanent(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void marksExactlyFourDamageWithoutDamagingOtherCreatures() {
        harness.addToBattlefield(player2, new UlamogsCrusher());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player2, "Ulamog's Crusher"));

        assertThat(findPermanent(player2, "Ulamog's Crusher").getMarkedDamage()).isEqualTo(4);
        assertThat(findPermanent(player2, "Grizzly Bears").getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Flame Slash");
    }

    @Test
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FlameSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
