package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
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

@CardUsed({DiplomaticRelations.class, GrizzlyBears.class, LlanowarElves.class})
class DiplomaticRelationsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature you control, grants vigilance, and deals its boosted power to an opponent creature")
    void boostsAndDealsPowerDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDiplomaticRelations(bears, harness.getPermanentId(player2, "Llanowar Elves"));

        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());
        castDiplomaticRelations(bears, harness.getPermanentId(player2, "Llanowar Elves"));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature you control as the second target")
    void secondTargetMustBeAnOpponentCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(source.getId(), other.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    @DisplayName("Damage uses the power after the boost and is not a fight")
    void damageUsesBoostedPowerWithoutRetaliation() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        castDiplomaticRelations(source, harness.getPermanentId(player2, "Grizzly Bears"));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(source.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The surviving first target is boosted when the second target leaves")
    void boostsFirstTargetWhenSecondTargetLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerGraveyards.get(player2.getId()).add(victim.getCard());

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        harness.assertInGraveyard(player1, "Diplomatic Relations");
    }

    @Test
    @DisplayName("No damage is dealt when the first target leaves before resolution")
    void dealsNoDamageWhenFirstTargetLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A first target that changes to an opponent's control is not boosted and deals no damage")
    void firstTargetMustRemainUnderYourControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerBattlefields.get(player2.getId()).add(source);

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isFalse();
        harness.assertOnBattlefield(player2, "Llanowar Elves");
        assertThat(victim.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A second target that changes to your control takes no damage while the first target is boosted")
    void secondTargetMustRemainUnderOpponentControl() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, List.of(source.getId(), victim.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerBattlefields.get(player1.getId()).add(victim);

        harness.passBothPriorities();

        assertThat(source.getPowerModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(victim.getMarkedDamage()).isZero();
        assertThat(victim.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, victim, Keyword.VIGILANCE)).isFalse();
    }

    private void castDiplomaticRelations(Permanent source, java.util.UUID victimId) {
        harness.setHand(player1, List.of(new DiplomaticRelations()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), victimId));
    }
}
