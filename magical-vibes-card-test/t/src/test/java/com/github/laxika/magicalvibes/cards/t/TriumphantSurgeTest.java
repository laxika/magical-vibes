package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KarametrasBlessing;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.r.RumblingSentry;
import com.github.laxika.magicalvibes.cards.s.SternDismissal;
import com.github.laxika.magicalvibes.cards.v.VoraciousTyphon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TriumphantSurge.class, VoraciousTyphon.class, RumblingSentry.class,
        NyxbornCourser.class, KarametrasBlessing.class, SternDismissal.class})
class TriumphantSurgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a creature with power exactly 4 and the spell's controller gains 3 life")
    void destroysLargeCreatureAndGainsLife() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());

        harness.setHand(player1, List.of(new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Voracious Typhon");
        harness.assertInGraveyard(player2, "Voracious Typhon");
        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Triumphant Surge");
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 4")
    void cannotTargetSmallCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RumblingSentry());

        harness.setHand(player1, List.of(new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 4 or greater");
    }

    @Test
    @DisplayName("Does not gain life when the target leaves the battlefield before resolution")
    void noLifeGainWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        harness.setHand(player1, List.of(new TriumphantSurge(), new SternDismissal()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Voracious Typhon");
        harness.assertNotOnBattlefield(player2, "Voracious Typhon");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Triumphant Surge");
    }

    @Test
    @DisplayName("Does not gain life if the target's power falls below 4 before resolution")
    void noLifeGainWhenTargetPowerDrops() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VoraciousTyphon());
        harness.setHand(player1, List.of(new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        target.setPersistentPowerModifier(-1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Voracious Typhon");
        harness.assertNotInGraveyard(player2, "Voracious Typhon");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Triumphant Surge");
    }

    @Test
    @DisplayName("Can destroy your own creature and gain life")
    void canDestroyOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VoraciousTyphon());
        harness.setHand(player1, List.of(new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Voracious Typhon");
        harness.assertInGraveyard(player1, "Voracious Typhon");
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("Uses modified power and gains life even when indestructible prevents destruction")
    void gainsLifeDespiteIndestructibleTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NyxbornCourser());
        harness.setHand(player1, List.of(new KarametrasBlessing(), new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nyxborn Courser");
        harness.assertNotInGraveyard(player1, "Nyxborn Courser");
        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Triumphant Surge");
    }

    @Test
    @DisplayName("Does not gain life when an opposing target gains hexproof in response")
    void noLifeGainWhenTargetGainsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NyxbornCourser());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new KarametrasBlessing()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.setHand(player1, List.of(new TriumphantSurge()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Nyxborn Courser");
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Triumphant Surge");
    }
}
