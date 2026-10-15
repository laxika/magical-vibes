package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.cards.i.IntoTheRoil;
import com.github.laxika.magicalvibes.cards.s.SpidersilkNet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeartstabberMosquito.class, GiantScorpion.class, SpidersilkNet.class, IntoTheRoil.class})
class HeartstabberMosquitoTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, the ETB ability does not destroy a creature")
    void withoutKickerDoesNotDestroyCreature() {
        harness.addToBattlefield(player2, new GiantScorpion());
        harness.castFromHand(player1, new HeartstabberMosquito(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heartstabber Mosquito");
        harness.assertOnBattlefield(player2, "Giant Scorpion");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("When kicked, the ETB ability destroys a target creature")
    void kickedDestroysCreature() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new HeartstabberMosquito()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Scorpion");
        harness.assertInGraveyard(player2, "Giant Scorpion");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player2, new SpidersilkNet());
        harness.setHand(player1, List.of(new HeartstabberMosquito()));
        addKickedMana();
        UUID targetId = harness.getPermanentId(player2, "Spidersilk Net");

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("An unkicked Mosquito needs no creature target")
    void withoutKickerOnEmptyBattlefield() {
        harness.castFromHand(player1, new HeartstabberMosquito(), "{3}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heartstabber Mosquito");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The kicked ability can destroy a creature its controller controls")
    void kickedDestroysFriendlyCreature() {
        harness.addToBattlefield(player1, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player1, "Giant Scorpion");
        harness.setHand(player1, List.of(new HeartstabberMosquito()));
        addKickedMana();

        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Giant Scorpion");
        harness.assertNotOnBattlefield(player1, "Giant Scorpion");
        harness.assertOnBattlefield(player1, "Heartstabber Mosquito");
    }

    @Test
    @DisplayName("The destruction ability does not affect a target that left the battlefield")
    void targetLeavesBeforeAbilityResolves() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new HeartstabberMosquito()));
        addKickedMana();
        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Giant Scorpion");
        harness.assertNotInGraveyard(player2, "Giant Scorpion");
        harness.assertOnBattlefield(player1, "Heartstabber Mosquito");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The kicked ability still destroys its target after Mosquito leaves")
    void sourceLeavesBeforeAbilityResolves() {
        harness.addToBattlefield(player2, new GiantScorpion());
        UUID targetId = harness.getPermanentId(player2, "Giant Scorpion");
        harness.setHand(player1, List.of(new HeartstabberMosquito()));
        addKickedMana();
        harness.castKickedCreature(player1, 0, targetId);
        harness.passBothPriorities();

        UUID mosquitoId = harness.getPermanentId(player1, "Heartstabber Mosquito");
        harness.setHand(player2, List.of(new IntoTheRoil()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, mosquitoId);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Heartstabber Mosquito");
        harness.assertInGraveyard(player2, "Giant Scorpion");
        harness.assertNotOnBattlefield(player2, "Giant Scorpion");
        assertThat(gd.stack).isEmpty();
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
