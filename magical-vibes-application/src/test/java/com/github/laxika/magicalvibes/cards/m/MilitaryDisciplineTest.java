package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoransEscape;
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

@CardUsed({MilitaryDiscipline.class, GrizzlyBears.class, FountainOfYouth.class,
        Disenchant.class, LoransEscape.class})
class MilitaryDisciplineTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Military Discipline attaches it, boosts the creature, and grants first strike")
    void resolvingAttachesAndAppliesEffects() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MilitaryDiscipline
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike granted by Military Discipline wears off at end of turn")
    void firstStrikeWearsOffAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Military Discipline cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("The +1/+0 bonus ends when Military Discipline leaves the battlefield")
    void boostEndsWhenAuraLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MilitaryDiscipline());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Flash allows Military Discipline to enchant an opponent's creature on their turn")
    void canBeCastOnOpponentsTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Military Discipline").getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The first strike trigger does not target the enchanted creature")
    void firstStrikeStillGrantedAfterEnchantedOpponentCreatureGainsHexproof() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.setHand(player2, List.of(new LoransEscape()));
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.castAndResolveInstant(player2, 0, bears.getId());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Destroying Military Discipline before its trigger resolves does not stop first strike")
    void firstStrikeGrantedAfterAuraDestroyedInResponse() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Military Discipline");
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.assertInGraveyard(player1, "Military Discipline");
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike remains until end of turn after the resolved Aura is destroyed")
    void firstStrikePersistsAfterAuraDestroyed() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MilitaryDiscipline()));
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        resolveAllTriggers();
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Military Discipline").getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }
}
