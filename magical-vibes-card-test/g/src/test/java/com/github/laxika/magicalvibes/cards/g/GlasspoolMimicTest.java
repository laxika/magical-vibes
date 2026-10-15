package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.n.NissaOfShadowedBoughs;
import com.github.laxika.magicalvibes.cards.s.SkyclaveCleric;
import com.github.laxika.magicalvibes.cards.s.SkyclaveBasilica;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GlasspoolMimic.class, GlasspoolShore.class, GrizzlyBears.class,
        SkyclaveCleric.class, SkyclaveBasilica.class, NissaOfShadowedBoughs.class})
class GlasspoolMimicTest extends BaseCardTest {

    @Test
    @DisplayName("Can copy a creature you control and keeps its copy exception subtypes")
    void copiesCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        Permanent mimic = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Glasspool Mimic"))
                .findFirst()
                .orElseThrow();
        assertThat(mimic.getCard().getPower()).isEqualTo(2);
        assertThat(mimic.getCard().getToughness()).isEqualTo(2);
        assertThat(mimic.getCard().getSubtypes())
                .contains(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Cannot copy a creature controlled by an opponent")
    void cannotCopyOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getOriginalCard().getName().equals("Glasspool Mimic"));
        harness.assertInGraveyard(player1, "Glasspool Mimic");
    }

    @Test
    @DisplayName("Glasspool Shore enters tapped and produces blue mana")
    void landFaceEntersTappedAndProducesBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlasspoolMimic()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("May decline to copy an available creature and die as a 0/0")
    void mayDeclineCopy() {
        harness.addToBattlefield(player1, new SkyclaveCleric());
        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Glasspool Mimic");
        harness.assertInGraveyard(player1, "Glasspool Mimic");
        harness.assertOnBattlefield(player1, "Skyclave Cleric");
    }

    @Test
    @DisplayName("Copying a creature retains its original subtypes and triggers its enters ability")
    void copiedEntersAbilityTriggers() {
        harness.addToBattlefield(player1, new SkyclaveCleric());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player1, "Skyclave Cleric"));
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        Permanent mimic = findPermanents(player1, "Skyclave Cleric").stream()
                .filter(permanent -> permanent.getOriginalCard().getName().equals("Glasspool Mimic"))
                .findFirst().orElseThrow();
        assertThat(mimic.getCard().getSubtypes()).containsExactlyInAnyOrder(
                CardSubtype.KOR, CardSubtype.CLERIC, CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE);
    }

    @Test
    @DisplayName("Copying an animated land does not give the noncreature copy creature subtypes")
    void copyingAnimatedLandDoesNotAddCreatureSubtypes() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GlasspoolMimic()));
        gs.playCard(gd, player1, 0, 1, null, null);
        Permanent shore = findPermanent(player1, "Glasspool Shore");
        harness.addToBattlefield(player1, new NissaOfShadowedBoughs());
        harness.activateAbility(player1, 1, 0, null, shore.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gqs.isCreature(gd, shore)).isTrue();

        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shore.getId());

        Permanent copy = findPermanents(player1, "Glasspool Shore").stream()
                .filter(permanent -> !permanent.getId().equals(shore.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(copy.isTapped()).isTrue();
        assertThat(copy.getCard().getSubtypes())
                .doesNotContain(CardSubtype.SHAPESHIFTER, CardSubtype.ROGUE, CardSubtype.ELEMENTAL);
    }

    @Test
    @DisplayName("Copying a creature does not copy its counters or tapped state")
    void doesNotCopyCountersOrTappedState() {
        harness.addToBattlefield(player1, new SkyclaveCleric());
        Permanent cleric = findPermanent(player1, "Skyclave Cleric");
        cleric.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        cleric.tap();
        harness.castFromHand(player1, new GlasspoolMimic(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, cleric.getId());

        Permanent copy = findPermanents(player1, "Skyclave Cleric").stream()
                .filter(permanent -> !permanent.getId().equals(cleric.getId()))
                .findFirst().orElseThrow();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, cleric)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cleric)).isEqualTo(6);
    }
}
