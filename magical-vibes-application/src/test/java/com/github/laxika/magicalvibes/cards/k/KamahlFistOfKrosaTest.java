package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KamahlFistOfKrosa.class, Forest.class, GrizzlyBears.class})
@DisplayName("Kamahl, Fist of Krosa")
class KamahlFistOfKrosaTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a target land into a 1/1 creature that is still a land")
    void animatesTargetLand() {
        Permanent kamahl = addPermanent(player1, new KamahlFistOfKrosa());
        Permanent land = addPermanent(player1, new Forest());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(kamahl), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(1);
    }

    @Test
    @DisplayName("The animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent kamahl = addPermanent(player1, new KamahlFistOfKrosa());
        Permanent land = addPermanent(player1, new Forest());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, battlefieldIndex(kamahl), 0, null, land.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gqs.isLand(gd, land)).isTrue();
    }

    @Test
    @DisplayName("Boosts your creatures and gives them trample, but not an opponent's creatures")
    void boostsOwnCreaturesAndGrantsTrample() {
        Permanent kamahl = addPermanent(player1, new KamahlFistOfKrosa());
        Permanent ownCreature = addPermanent(player1, new GrizzlyBears());
        Permanent opponentCreature = addPermanent(player2, new GrizzlyBears());
        int kamahlPower = kamahl.getEffectivePower();
        int kamahlToughness = kamahl.getEffectiveToughness();
        int ownPower = ownCreature.getEffectivePower();
        int ownToughness = ownCreature.getEffectiveToughness();
        int opponentPower = opponentCreature.getEffectivePower();
        int opponentToughness = opponentCreature.getEffectiveToughness();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, battlefieldIndex(kamahl), 1, null, null);
        harness.passBothPriorities();

        assertThat(kamahl.getEffectivePower()).isEqualTo(kamahlPower + 3);
        assertThat(kamahl.getEffectiveToughness()).isEqualTo(kamahlToughness + 3);
        assertThat(kamahl.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(ownCreature.getEffectivePower()).isEqualTo(ownPower + 3);
        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(ownToughness + 3);
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentCreature.getEffectivePower()).isEqualTo(opponentPower);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(opponentToughness);
        assertThat(opponentCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent kamahl = addPermanent(player1, new KamahlFistOfKrosa());
        Permanent ownCreature = addPermanent(player1, new GrizzlyBears());
        int kamahlPower = kamahl.getEffectivePower();
        int ownPower = ownCreature.getEffectivePower();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, battlefieldIndex(kamahl), 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(kamahl.getEffectivePower()).isEqualTo(kamahlPower);
        assertThat(ownCreature.getEffectivePower()).isEqualTo(ownPower);
        assertThat(kamahl.hasKeyword(Keyword.TRAMPLE)).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The first ability cannot target a nonland permanent")
    void cannotTargetNonland() {
        Permanent kamahl = addPermanent(player1, new KamahlFistOfKrosa());
        Permanent creature = addPermanent(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(kamahl), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addPermanent(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
