package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionBlack;
import com.github.laxika.magicalvibes.cards.e.EnergyStorm;
import com.github.laxika.magicalvibes.cards.f.Fork;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukWildspeaker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DrainLife.class, CircleOfProtectionBlack.class, GrizzlyBears.class, Plains.class,
        GarrukWildspeaker.class, EnergyStorm.class, FurnaceOfRath.class, Fork.class})
class DrainLifeTest extends BaseCardTest {

    @Test
    @DisplayName("X=3 at a player deals 3 damage and controller gains 3 life")
    void drainsPlayer() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 5); // {X}{1}{B}, X=3
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("X=2 kills a 2/2 and controller gains 2 life")
    void killsCreatureAndGainsLife() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 4); // {2}{1}{B}
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 2, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cast at a land is rejected")
    void castAtLandIsRejected() {
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, plainsId))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Drain Life");
    }

    @Test
    @DisplayName("X cannot be paid with non-black mana")
    void cannotPayXWithNonBlackMana() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 1); // {B}
        harness.addMana(player1, ManaColor.COLORLESS, 3); // {X}{1}

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Life gain is capped by a target player's life total before damage")
    void lifeGainCappedByPlayerLife() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 7); // X=5
        harness.setLife(player1, 20);
        harness.setLife(player2, 3);

        harness.castAndResolveSorcery(player1, 0, 5, player2.getId());

        harness.assertLife(player2, -2);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Life gain is capped by a creature's toughness before damage")
    void lifeGainCappedByCreatureToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=4
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, 4, bearsId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life gain is capped by a planeswalker's loyalty before damage")
    void lifeGainCappedByPlaneswalkerLoyalty() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GarrukWildspeaker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 2);
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 6); // X=4
        harness.setLife(player1, 20);

        harness.castAndResolveSorcery(player1, 0, 4, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Garruk Wildspeaker");
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Prevented damage produces no life gain")
    void preventedDamageDoesNotGrantLife() {
        harness.addToBattlefield(player2, new EnergyStorm());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 5); // X=3
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    void increasedDamageGrantsLifeBeyondX() {
        harness.addToBattlefield(player2, new FurnaceOfRath());
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, player2.getId());

        harness.assertLife(player2, 14);
        harness.assertLife(player1, 26);
    }

    @Test
    void copiedDrainLifeStillGrantsLife() {
        DrainLife drainLife = new DrainLife();
        harness.setHand(player1, List.of(drainLife));
        harness.setHand(player2, List.of(new Fork()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, drainLife.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    void zeroXDealsNoDamageAndGrantsNoLife() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, player2.getId());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void selfTargetingRestoresLifeBeforeStateBasedActions() {
        harness.setHand(player1, List.of(new DrainLife()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.setLife(player1, 3);

        harness.castAndResolveSorcery(player1, 0, 3, player1.getId());

        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("Activated damage prevention produces no life gain")
    void activatedPreventionProducesNoLifeGain() {
        Permanent circle = harness.addToBattlefieldAndReturn(player2, new CircleOfProtectionBlack());
        DrainLife drainLife = new DrainLife();
        harness.setHand(player1, List.of(drainLife));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        int circleIndex = gd.playerBattlefields.get(player2.getId()).indexOf(circle);
        harness.activateAbility(player2, circleIndex, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, drainLife.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }
}
