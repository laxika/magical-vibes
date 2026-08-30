package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SkredTest extends BaseCardTest {

    @Test
    @DisplayName("Skred deals damage equal to the snow permanents its controller controls")
    void dealsDamageEqualToControlledSnowPermanents() {
        addSnowPermanent(player1);
        addSnowPermanent(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSkred(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Skred counts only snow permanents controlled by its controller")
    void countsOnlyControllersSnowPermanents() {
        addSnowPermanent(player1);
        addSnowPermanent(player2);
        addSnowPermanent(player2);
        harness.addToBattlefield(player2, new GrizzlyBears());

        castSkred(harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Skred counts snow permanents at resolution")
    void countsSnowPermanentsAtResolution() {
        Permanent firstSnowPermanent = addSnowPermanent(player1);
        addSnowPermanent(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).remove(firstSnowPermanent);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Skred cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addSnowPermanent(Player owner) {
        Permanent snowPermanent = harness.addToBattlefieldAndReturn(owner, new Forest());
        TestCards.mutableCard(snowPermanent).setSupertypes(EnumSet.of(CardSupertype.SNOW));
        return snowPermanent;
    }

    private void castSkred(UUID targetId) {
        harness.setHand(player1, List.of(new Skred()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
