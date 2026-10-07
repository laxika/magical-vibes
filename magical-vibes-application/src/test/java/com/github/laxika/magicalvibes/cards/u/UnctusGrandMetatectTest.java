package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.cards.t.TamiyosImmobilizer;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnctusGrandMetatect.class, Forest.class, GrizzlyBears.class, Ornithopter.class,
        ProdigalSorcerer.class, TamiyosImmobilizer.class})
class UnctusGrandMetatectTest extends BaseCardTest {

    @Test
    @DisplayName("Other artifact creatures you control get +1/+1")
    void buffsOtherArtifactCreatures() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent ornithopter = findPermanent(player1, "Ornithopter");
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Other blue creatures draw then discard when they become tapped")
    void grantsLootTriggerToOtherBlueCreatures() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, null, player2.getId());
        assertThat(sorcerer.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The activated ability makes a creature blue and an artifact until end of turn")
    void makesCreatureBlueAndArtifactUntilEndOfTurn() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).contains(CardColor.BLUE, CardColor.GREEN);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).doesNotContain(CardColor.BLUE);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The activated ability only targets a creature you control")
    void activatedAbilityRequiresCreatureYouControl() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    @DisplayName("The activated ability can only be used at sorcery speed")
    void activatedAbilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Tapping a blue noncreature artifact does not trigger looting")
    void blueNoncreatureDoesNotLoot() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        Permanent immobilizer = harness.enterBattlefieldAndReturn(player1, new TamiyosImmobilizer());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, null, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player2, "Grizzly Bears").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tapped blue creature is the source of its granted loot ability")
    void tappedCreatureIsLootAbilitySource() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .toList()).singleElement().satisfies(entry -> {
                    assertThat(entry.getCard()).isSameAs(sorcerer.getCard());
                    assertThat(entry.getSourcePermanentId()).isEqualTo(sorcerer.getId());
                });
    }

    @Test
    @DisplayName("Unctus does not boost itself or opposing artifact creatures")
    void excludesSelfAndOpponentsFromBoost() {
        Permanent unctus = harness.addToBattlefieldAndReturn(player1, new UnctusGrandMetatect());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        assertThat(gqs.getEffectivePower(gd, unctus)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unctus)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingArtifact)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, opposingArtifact)).isEqualTo(2);
    }

    @Test
    @DisplayName("The Phyrexian activation cost can be paid with two life")
    void paysLifeForActivation() {
        harness.addToBattlefield(player1, new UnctusGrandMetatect());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        assertThat(gqs.isArtifact(gd, findPermanent(player1, "Grizzly Bears"))).isTrue();
    }

    @Test
    @DisplayName("Tapping Unctus and a nonblue creature does not trigger looting")
    void selfAndNonblueCreatureDoNotLoot() {
        addCreatureReady(player1, new UnctusGrandMetatect());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0, 1));

        assertThat(findPermanent(player1, "Unctus, Grand Metatect").isTapped()).isTrue();
        assertThat(findPermanent(player1, "Grizzly Bears").isTapped()).isTrue();
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
