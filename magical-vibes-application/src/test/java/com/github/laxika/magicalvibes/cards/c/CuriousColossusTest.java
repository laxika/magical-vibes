package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HorseshoeCrab;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CuriousColossus.class, SerraAngel.class, GrizzlyBears.class, FountainOfYouth.class,
        Unsummon.class, HorseshoeCrab.class})
class CuriousColossusTest extends BaseCardTest {

    @Test
    @DisplayName("Weakens all creatures controlled by the targeted opponent and adds Coward")
    void affectsTargetOpponentsCreatures() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        Permanent ownAngel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());

        castCuriousColossus(player2.getId());

        assertThat(angel.getEffectivePower()).isEqualTo(1);
        assertThat(angel.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel))
                .contains(CardSubtype.ANGEL, CardSubtype.COWARD);
        assertThat(bears.getEffectivePower()).isEqualTo(1);
        assertThat(bears.getEffectiveToughness()).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.COWARD);
        assertThat(ownAngel.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownAngel, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("All three changes persist through cleanup into the next turn")
    void changesPersistIntoNextTurn() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castCuriousColossus(player2.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel))
                .contains(CardSubtype.ANGEL, CardSubtype.COWARD);
    }

    @Test
    @DisplayName("Creatures entering after resolution are unaffected")
    void laterCreaturesAreUnaffected() {
        Permanent original = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        castCuriousColossus(player2.getId());
        Permanent newcomer = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, newcomer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, newcomer)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, newcomer, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, newcomer)).doesNotContain(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("Changes persist after Curious Colossus leaves and through cleanup")
    void changesPersistWithoutSource() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castCuriousColossus(player2.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Curious Colossus"));
        harness.assertNotOnBattlefield(player1, "Curious Colossus");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, angel)).contains(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("Power and toughness counters still apply above the new base")
    void countersStillApply() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        castCuriousColossus(player2.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent with no creatures is still a legal target")
    void canTargetOpponentWithoutCreatures() {
        harness.addToBattlefield(player2, new FountainOfYouth());

        castCuriousColossus(player2.getId());

        harness.assertOnBattlefield(player1, "Curious Colossus");
        harness.assertOnBattlefield(player2, "Fountain of Youth");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Affected creatures cannot activate their printed abilities")
    void removesActivatedAbilities() {
        Permanent crab = harness.addToBattlefieldAndReturn(player2, new HorseshoeCrab());
        crab.setTapped(true);
        harness.addMana(player2, ManaColor.BLUE, 1);

        castCuriousColossus(player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(crab.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Leaving and returning makes an affected creature a new unaffected object")
    void returnedCreatureIsUnaffected() {
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        castCuriousColossus(player2.getId());

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, angel.getId());
        harness.assertNotOnBattlefield(player2, "Serra Angel");
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(angel.getCard()));
        harness.addMana(player2, ManaColor.WHITE, 5);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent returnedAngel = findPermanent(player2, "Serra Angel");
        assertThat(gqs.getEffectivePower(gd, returnedAngel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, returnedAngel)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, returnedAngel, Keyword.FLYING)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, returnedAngel)).doesNotContain(CardSubtype.COWARD);
    }

    @Test
    @DisplayName("Cannot target its own controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new CuriousColossus()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCuriousColossus(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new CuriousColossus()));
        harness.addMana(player1, ManaColor.WHITE, 7);
        harness.castCreature(player1, 0, List.of(targetPlayerId));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
