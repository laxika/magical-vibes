package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AcrobaticCheerleader;
import com.github.laxika.magicalvibes.cards.e.Exorcise;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShardmagesRescue.class, AcrobaticCheerleader.class, Murder.class, Exorcise.class})
class ShardmagesRescueTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and hexproof when Shardmage's Rescue enters")
    void grantsBoostAndHexproofWhenItEnters() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        castRescue(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof lasts only through the turn Shardmage's Rescue enters")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        castRescue(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Shardmage's Rescue can enchant only a creature you control")
    void cannotEnchantOpponentCreature() {
        harness.addToBattlefield(player2, new AcrobaticCheerleader());
        harness.setHand(player1, List.of(new ShardmagesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID opponentCreature = harness.getPermanentId(player2, "Acrobatic Cheerleader");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash rescues a creature from removal on the opponent's turn")
    void flashProtectsAgainstRemovalAlreadyOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, bears.getId());

        castRescue(bears);
        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Acrobatic Cheerleader");
        harness.assertInGraveyard(player2, "Murder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Hexproof remains active during the turn's end step")
    void hexproofStillProtectsDuringEndStep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        castRescue(bears);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThatThrownBy(() -> harness.castInstant(player2, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The creature's controller can target it through hexproof")
    void controllerCanEnchantHexproofCreatureAgain() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        castRescue(bears);
        castRescue(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Removing the Aura immediately removes its boost and hexproof")
    void removingAuraRemovesBothBenefits() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AcrobaticCheerleader());
        castRescue(bears);
        UUID rescue = harness.getPermanentId(player1, "Shardmage's Rescue");
        harness.setHand(player1, List.of(new Exorcise()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveSorcery(player1, 0, rescue);

        harness.assertNotOnBattlefield(player1, "Shardmage's Rescue");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    private void castRescue(Permanent creature) {
        harness.setHand(player1, List.of(new ShardmagesRescue()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
