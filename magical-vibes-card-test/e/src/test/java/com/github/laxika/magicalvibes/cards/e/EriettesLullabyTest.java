package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GetawayGlamer;
import com.github.laxika.magicalvibes.cards.g.GiantBeaver;
import com.github.laxika.magicalvibes.cards.g.GoldPan;
import com.github.laxika.magicalvibes.cards.t.TakeUpTheShield;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EriettesLullaby.class, GiantBeaver.class, GoldPan.class, GetawayGlamer.class, TakeUpTheShield.class})
class EriettesLullabyTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a tapped creature and gains 2 life")
    void destroysTappedCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());
        target.tap();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EriettesLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Beaver");
        harness.assertInGraveyard(player2, "Giant Beaver");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent validTarget = harness.addToBattlefieldAndReturn(player1, new GiantBeaver());
        validTarget.tap();
        Permanent untappedTarget = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());

        harness.setHand(player1, List.of(new EriettesLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, untappedTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Can destroy a tapped creature you control")
    void destroysOwnTappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GiantBeaver());
        target.tap();
        prepareLullaby();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Giant Beaver");
        harness.assertInGraveyard(player1, "Giant Beaver");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a tapped noncreature permanent")
    void cannotTargetTappedNoncreature() {
        Permanent validTarget = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());
        validTarget.tap();
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new GoldPan());
        noncreature.tap();
        prepareLullaby();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped creature");
    }

    @Test
    @DisplayName("Does not destroy or gain life if the target untaps before resolution")
    void noLifeGainWhenTargetUntaps() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());
        target.tap();
        prepareLullaby();

        harness.castSorcery(player1, 0, target.getId());
        target.untap();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Beaver");
        harness.assertNotInGraveyard(player2, "Giant Beaver");
        harness.assertInGraveyard(player1, "Eriette's Lullaby");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not gain life if the target leaves the battlefield before resolution")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());
        target.tap();
        prepareLullaby();
        harness.setHand(player2, List.of(new GetawayGlamer()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castModalInstantWithModes(player2, 0, 1, 2, new int[]{0}, List.of(target.getId()));
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Giant Beaver");
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Giant Beaver");
        harness.assertInGraveyard(player1, "Eriette's Lullaby");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains life even when the tapped target is indestructible")
    void gainsLifeWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantBeaver());
        target.tap();
        prepareLullaby();
        harness.setHand(player2, List.of(new TakeUpTheShield()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Giant Beaver");
        harness.assertNotInGraveyard(player2, "Giant Beaver");
        harness.assertInGraveyard(player1, "Eriette's Lullaby");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }

    private void prepareLullaby() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new EriettesLullaby()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
