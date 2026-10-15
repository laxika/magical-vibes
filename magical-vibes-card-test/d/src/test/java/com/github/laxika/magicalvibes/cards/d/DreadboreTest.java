package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.g.GolgariCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RootbornDefenses;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dreadbore.class, GarrukWildspeaker.class, GrizzlyBears.class, Plains.class,
        GolgariCharm.class, RootbornDefenses.class, DramaticRescue.class})
class DreadboreTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target creature")
    void destroysCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, List.of(targetId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Destroys target planeswalker")
    void destroysPlaneswalker() {
        Permanent planeswalker = addReadyPlaneswalker(player2, 3);
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(planeswalker.getId()));

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertInGraveyard(player2, "Garruk Wildspeaker");
    }

    @Test
    @DisplayName("Rejects non-creature, non-planeswalker targets")
    void rejectsLandTarget() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        UUID landId = harness.getPermanentId(player2, "Plains");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(landId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster without destroying other creatures")
    void destroysOwnCreatureOnly() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, List.of(target.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A regeneration shield prevents destruction")
    void allowsRegeneration() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.setHand(player2, List.of(new GolgariCharm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.castInstant(player2, 0, 2, null);
        harness.passBothPriorities();
        assertThat(target.getRegenerationShield()).isEqualTo(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Dreadbore");
    }

    @Test
    @DisplayName("Indestructible prevents destruction")
    void respectsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.setHand(player2, List.of(new RootbornDefenses()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player2, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dreadbore");
    }

    @Test
    @DisplayName("Does not destroy another creature when the target leaves before resolution")
    void targetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Dreadbore()));
        harness.setHand(player2, List.of(new DramaticRescue()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Dreadbore");
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukWildspeaker());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        return perm;
    }
}
