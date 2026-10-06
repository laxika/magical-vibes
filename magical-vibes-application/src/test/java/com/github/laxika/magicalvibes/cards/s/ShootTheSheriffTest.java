package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.d.DeadeyeDuelist;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MalcolmTheEyes;
import com.github.laxika.magicalvibes.cards.n.NezumiLinkbreaker;
import com.github.laxika.magicalvibes.cards.o.OutlawMedic;
import com.github.laxika.magicalvibes.cards.p.PricklyPair;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShootTheSheriff.class, GrizzlyBears.class, OutlawMedic.class, Forest.class,
        ArmoredArmadillo.class, DeadeyeDuelist.class, MalcolmTheEyes.class,
        NezumiLinkbreaker.class, PricklyPair.class})
class ShootTheSheriffTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target non-outlaw creature")
    void destroysTargetNonOutlawCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an outlaw creature")
    void cannotTargetOutlawCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new OutlawMedic());

        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-outlaw creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-outlaw creature");
    }

    @ParameterizedTest
    @MethodSource("otherOutlaws")
    void cannotTargetAnyOtherOutlawType(Card card) {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-outlaw creature");
    }

    static Stream<Card> otherOutlaws() {
        return Stream.of(new DeadeyeDuelist(), new PricklyPair(),
                new MalcolmTheEyes(), new NezumiLinkbreaker());
    }

    @Test
    void canDestroyOwnNonOutlawCreatureWithWard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Armored Armadillo");
        harness.assertInGraveyard(player1, "Armored Armadillo");
    }

    @Test
    void doesNotDestroyTargetThatBecomesAnOutlawBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ShootTheSheriff()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, creature.getId());
        creature.getGrantedSubtypes().add(CardSubtype.ROGUE);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Shoot the Sheriff");
    }
}
