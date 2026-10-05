package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Fireball;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.l.Luminollusk;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KulrathMystic.class, GrizzlyBears.class, JacesIngenuity.class, Fireball.class, Luminollusk.class})
class KulrathMysticTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell with mana value 4 or greater gives +2/+0 and vigilance")
    void highManaValueSpellBoostsAndGrantsVigilance() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Casting a spell with mana value less than 4 does not trigger Kulrath Mystic")
    void lowManaValueSpellDoesNotTrigger() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void boostAndVigilanceWearOffAtEndOfTurn() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player1, 0);

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(mystic.getPowerModifier()).isEqualTo(0);
        assertThat(mystic.getToughnessModifier()).isEqualTo(0);
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A creature spell with mana value exactly four triggers before entering")
    void exactlyFourManaValueTriggers() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new Luminollusk()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.getToughnessModifier()).isZero();
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();
        harness.assertNotOnBattlefield(player1, "Luminollusk");
    }

    @Test
    @DisplayName("An opponent's qualifying spell does not trigger")
    void opponentSpellDoesNotTrigger() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player2, List.of(new JacesIngenuity()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castAndResolveInstant(player2, 0);

        assertThat(mystic.getPowerModifier()).isZero();
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Each qualifying spell adds another boost to its own Mystic")
    void repeatedTriggersAccumulateOnlyOnSource() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        Permanent opponentMystic = harness.addToBattlefieldAndReturn(player2, new KulrathMystic());
        harness.setHand(player1, List.of(new JacesIngenuity(), new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);

        assertThat(mystic.getPowerModifier()).isEqualTo(4);
        assertThat(mystic.getToughnessModifier()).isZero();
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();
        assertThat(opponentMystic.getPowerModifier()).isZero();
        assertThat(opponentMystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("X contributes to the mana value of the spell on the stack")
    void xSpellReachingFourManaValueTriggers() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(mystic.getPowerModifier()).isEqualTo(2);
        assertThat(mystic.getToughnessModifier()).isZero();
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("An X spell below mana value four does not trigger")
    void xSpellBelowFourManaValueDoesNotTrigger() {
        Permanent mystic = harness.addToBattlefieldAndReturn(player1, new KulrathMystic());
        harness.setHand(player1, List.of(new Fireball()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveSorcery(player1, 0, 2, player2.getId());

        assertThat(mystic.getPowerModifier()).isZero();
        assertThat(mystic.getToughnessModifier()).isZero();
        assertThat(mystic.hasKeyword(Keyword.VIGILANCE)).isFalse();
    }
}
