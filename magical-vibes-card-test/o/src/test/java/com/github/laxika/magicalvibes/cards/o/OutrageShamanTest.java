package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.h.HeartlashCinder;
import com.github.laxika.magicalvibes.cards.r.RiseOfTheHobgoblins;
import com.github.laxika.magicalvibes.cards.s.SpringjackShepherd;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutrageShaman.class, HeartlashCinder.class, SpringjackShepherd.class,
        AltarOfThePantheon.class, RiseOfTheHobgoblins.class})
class OutrageShamanTest extends BaseCardTest {

    private Permanent addTarget(Card targetCard) {
        targetCard.setToughness(5);
        return harness.addToBattlefieldAndReturn(player2, targetCard);
    }

    private void castShaman(Permanent target) {
        harness.setHand(player1, List.of(new OutrageShaman()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castCreature(player1, 0, target.getId());
    }

    private void castAndResolveShaman(Permanent target) {
        castShaman(target);
        harness.passBothPriorities(); // Resolve creature → ETB triggers
        harness.passBothPriorities(); // Resolve ETB
    }

    @Test
    @DisplayName("ETB damage equals red symbols in own cost when it is the only permanent (self counts)")
    void etbDamageFromOwnRedSymbols() {
        // Outrage Shaman {3}{R}{R} = 2 red symbols on the battlefield when the trigger resolves.
        Permanent target = addTarget(new SpringjackShepherd());

        castAndResolveShaman(target);

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Red symbols across all controlled permanents add up (Heartlash Cinder + self = 3)")
    void etbCountsRedSymbolsAcrossPermanents() {
        // Heartlash Cinder {1}{R} = 1 red symbol; Outrage Shaman {3}{R}{R} = 2. Total = 3.
        harness.addToBattlefield(player1, new HeartlashCinder());
        Permanent target = addTarget(new SpringjackShepherd());

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-red permanents contribute no symbols")
    void etbIgnoresNonRedSymbols() {
        // Springjack Shepherd {3}{W} on your side = 0 red symbols; only the Shaman's own {R}{R} counts.
        harness.addToBattlefield(player1, new SpringjackShepherd());
        Permanent target = addTarget(new SpringjackShepherd());

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's red symbols do not contribute")
    void etbIgnoresOpponentsRedSymbols() {
        // The opponent's Heartlash Cinder has one red symbol, but only the Shaman's two count.
        Permanent target = addTarget(new HeartlashCinder());

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts red symbols from permanents added before the ETB trigger resolves")
    void etbCountsAtTriggerResolution() {
        Permanent target = addTarget(new SpringjackShepherd());

        castShaman(target);
        harness.passBothPriorities(); // Resolve creature → ETB triggers
        harness.addToBattlefield(player1, new HeartlashCinder());
        harness.passBothPriorities(); // Resolve ETB with the new permanent on the battlefield

        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @CardUsed(AltarOfThePantheon.class)
    @DisplayName("Devotion modifiers do not add extra Chroma symbols")
    void etbDoesNotCountDevotionModifiers() {
        // Altar of the Pantheon increases devotion, but its {3} cost has no red symbols.
        harness.addToBattlefield(player1, new AltarOfThePantheon());
        Permanent target = addTarget(new SpringjackShepherd());

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @CardUsed(RiseOfTheHobgoblins.class)
    @DisplayName("Hybrid red symbols on noncreature permanents contribute once each")
    void etbCountsHybridSymbolsOnEnchantments() {
        harness.addToBattlefield(player1, new RiseOfTheHobgoblins());
        Permanent target = addTarget(new SpringjackShepherd());

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("The trigger can damage a creature controlled by its controller")
    void etbCanTargetOwnCreature() {
        SpringjackShepherd shepherd = new SpringjackShepherd();
        shepherd.setToughness(5);
        Permanent target = harness.addToBattlefieldAndReturn(player1, shepherd);

        castAndResolveShaman(target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("A Shaman that leaves before resolution contributes no symbols")
    void etbDealsZeroWhenSourceLeavesAndNoRedSymbolsRemain() {
        Permanent target = addTarget(new SpringjackShepherd());
        castShaman(target);
        harness.passBothPriorities();
        Permanent shaman = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A departed Shaman's trigger still deals damage from remaining red symbols")
    void etbResolvesAfterSourceLeaves() {
        Permanent target = addTarget(new SpringjackShepherd());
        castShaman(target);
        harness.passBothPriorities();
        Permanent shaman = gd.playerBattlefields.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(shaman);
        gd.playerGraveyards.get(player1.getId()).add(shaman.getCard());
        harness.addToBattlefield(player1, new HeartlashCinder());

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }
}
