package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.s.SqueakingPieSneak;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarshFlitter.class, SqueakingPieSneak.class, BoggartShenanigans.class})
class MarshFlitterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates two 1/1 Goblin Rogue tokens")
    void etbCreatesTwoGoblinRogueTokens() {
        harness.setHand(player1, List.of(new MarshFlitter()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Goblin Rogue"))
                .hasSize(2)
                .allSatisfy(p -> {
                    assertThat(p.getCard().getPower()).isEqualTo(1);
                    assertThat(p.getCard().getToughness()).isEqualTo(1);
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.BLACK);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.GOBLIN, CardSubtype.ROGUE);
                });
    }

    @Test
    @DisplayName("Sacrificing a Goblin sets base power and toughness to 3/3")
    void sacrificeGoblinSetsBaseThreeThree() {
        Permanent flitter = addMarshFlitterReady(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Squeaking Pie Sneak");
        harness.assertInGraveyard(player1, "Squeaking Pie Sneak");
        assertThat(flitter.getEffectivePower()).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(flitter.getEffectivePower()).isEqualTo(3);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Base power/toughness override resets at end of turn")
    void baseOverrideResetsAtEndOfTurn() {
        Permanent flitter = addMarshFlitterReady(player1);
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(flitter.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(flitter.getEffectivePower()).isEqualTo(1);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate with no Goblin to sacrifice")
    void cannotActivateWithoutGoblin() {
        addMarshFlitterReady(player1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature Goblin can pay the sacrifice cost")
    void canSacrificeGoblinEnchantment() {
        Permanent flitter = harness.addToBattlefieldAndReturn(player1, new MarshFlitter());
        harness.addToBattlefield(player1, new BoggartShenanigans());

        harness.activateAbility(player1, 0, null, null);
        harness.assertNotOnBattlefield(player1, "Boggart Shenanigans");
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        harness.passBothPriorities();

        assertThat(flitter.getEffectivePower()).isEqualTo(3);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's Goblin")
    void cannotSacrificeOpponentsGoblin() {
        harness.addToBattlefield(player1, new MarshFlitter());
        harness.addToBattlefield(player2, new SqueakingPieSneak());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Squeaking Pie Sneak");
    }

    @Test
    @DisplayName("A summoning-sick Marsh Flitter can activate without tapping")
    void canActivateWhileSummoningSick() {
        Permanent flitter = harness.addToBattlefieldAndReturn(player1, new MarshFlitter());
        harness.addToBattlefield(player1, new SqueakingPieSneak());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(flitter.isTapped()).isFalse();
        assertThat(flitter.getEffectivePower()).isEqualTo(3);
        assertThat(flitter.getEffectiveToughness()).isEqualTo(3);
    }

    private Permanent addMarshFlitterReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MarshFlitter());
        perm.setSummoningSick(false);
        return perm;
    }

}
