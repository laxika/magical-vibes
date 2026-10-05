package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MakeYourMark.class, GrizzlyBears.class, AirElemental.class, FountainOfYouth.class, Shock.class})
class MakeYourMarkTest extends BaseCardTest {

    private void resolveStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 10) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Gives target creature +1/+0 and creates a Spirit when it dies this turn")
    void boostsAndCreatesSpiritWhenTargetDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);

        harness.castInstant(player1, 0, targetId);
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        Permanent spirit = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spirit"))
                .findFirst()
                .orElseThrow();
        assertThat(spirit.getEffectivePower()).isEqualTo(3);
        assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The target's power boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new MakeYourMark()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = target.getId();
        harness.castInstant(player1, 0, targetId);
        resolveStack();

        assertThat(target.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new MakeYourMark()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A death after the turn ends does not create a Spirit")
    void delayedTriggerExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Spirit");
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("Two resolved copies each create a Spirit when the same creature dies")
    void multipleCopiesCreateSeparateTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();
        harness.castInstant(player1, 0, target.getId());
        resolveStack();
        assertThat(target.getEffectivePower()).isEqualTo(4);

        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A target dying before resolution does not create a Spirit")
    void targetDiesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Make Your Mark");
        harness.assertNotOnBattlefield(player1, "Spirit");
        harness.assertNotOnBattlefield(player2, "Spirit");
    }

    @Test
    @DisplayName("A marked creature token dying also creates a Spirit")
    void dyingTokenCreatesSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new Shock(), new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        UUID spiritId = harness.getPermanentId(player1, "Spirit");
        harness.castInstant(player1, 0, spiritId);
        resolveStack();
        harness.castInstant(player1, 0, spiritId);
        resolveStack();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(spiritId))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .singleElement().satisfies(spirit -> {
                    assertThat(spirit.getEffectivePower()).isEqualTo(3);
                    assertThat(spirit.getEffectiveToughness()).isEqualTo(2);
                    assertThat(spirit.getCard().getColors())
                            .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
                    assertThat(spirit.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
                });
    }

    @Test
    @DisplayName("The delayed trigger still works during the end step")
    void deathDuringEndStepCreatesSpirit() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MakeYourMark(), new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveStack();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Spirit"))
                .hasSize(1);
    }
}
