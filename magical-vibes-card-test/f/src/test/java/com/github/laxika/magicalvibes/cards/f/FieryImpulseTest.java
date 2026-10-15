package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FieryImpulse.class, HillGiant.class, GrizzlyBears.class, Shock.class, LightningBolt.class, LavaAxe.class})
class FieryImpulseTest extends BaseCardTest {

    @Test
    @DisplayName("Without spell mastery it deals only 2 damage")
    void dealsTwoDamageWithoutSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("With spell mastery it deals 3 damage instead, killing a 3/3")
    void dealsThreeDamageWithSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock(), new LightningBolt()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Creature cards in the graveyard do not enable spell mastery")
    void creatureCardsDoNotEnableSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        UUID targetId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target a creature its controller owns")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Two sorceries enable spell mastery")
    void sorceriesEnableSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new LavaAxe(), new LavaAxe()));

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("An instant and a sorcery together enable spell mastery")
    void mixedTypesEnableSpellMastery() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock(), new LavaAxe()));

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Opponent's graveyard does not enable spell mastery")
    void ignoresOpponentsGraveyard() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setGraveyard(player2, List.of(new Shock(), new LavaAxe()));

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Hill Giant").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Spell mastery can become enabled by a spell resolving in response")
    void checksSpellMasteryAtResolution() {
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new FieryImpulse(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setGraveyard(player1, List.of(new LavaAxe()));

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Hill Giant"));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
