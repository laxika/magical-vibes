package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.d.DrannithMagistrate;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartlessAct;
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

@CardUsed({PatagiaTiger.class, EliteVanguard.class, GrizzlyBears.class,
        DrannithMagistrate.class, HeartlessAct.class, ArtificialEvolution.class, Bitterblossom.class})
class PatagiaTigerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a Human creature you control +2/+2 until end of turn")
    void etbBoostsHumanYouControl() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        castPatagiaTiger(human.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new EliteVanguard());
        castPatagiaTiger(human.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's Human")
    void cannotTargetOpponentsHuman() {
        Permanent human = harness.addToBattlefieldAndReturn(player2, new EliteVanguard());
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, human.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, creature.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Tiger can enter without a legal target and no ETB ability remains on the stack")
    void canEnterWithoutLegalTarget() {
        harness.castFromHand(player1, new PatagiaTiger(), "{4}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patagia Tiger");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void boostResolvesAfterTigerIsDestroyed() {
        Permanent human = harness.addToBattlefieldAndReturn(player1, new DrannithMagistrate());
        castPatagiaTiger(human.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, 0, harness.getPermanentId(player1, "Patagia Tiger"));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Patagia Tiger");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(5);
    }

    @Test
    void removedTargetDoesNotRedirectBoostToAnotherHuman() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DrannithMagistrate());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new DrannithMagistrate());
        castPatagiaTiger(target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new HeartlessAct()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetNoncreatureHumanPermanent() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "HUMAN");
        assertThat(gqs.hasEffectiveSubtype(gd, enchantment, CardSubtype.HUMAN)).isTrue();

        castPatagiaTiger(enchantment.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(enchantment.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Patagia Tiger");
        harness.assertOnBattlefield(player1, "Bitterblossom");
        assertThat(gd.stack).isEmpty();
    }

    private void castPatagiaTiger(UUID targetId) {
        harness.setHand(player1, List.of(new PatagiaTiger()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0, targetId);
    }
}
