package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WallOfKelp.class})
class WallOfKelpTest extends BaseCardTest {

    @Test
    @DisplayName("Activating creates a 0/1 blue Plant Wall token with defender named Kelp")
    void createsKelpToken() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wall.isTapped()).isTrue();
        Permanent kelp = findPermanent(player1, "Kelp");
        assertThat(kelp.getCard().isToken()).isTrue();
        assertThat(kelp.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(kelp.getCard().getPower()).isEqualTo(0);
        assertThat(kelp.getCard().getToughness()).isEqualTo(1);
        assertThat(kelp.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(kelp.getCard().getSubtypes()).contains(CardSubtype.PLANT, CardSubtype.WALL);
        assertThat(kelp.getCard().getKeywords()).contains(Keyword.DEFENDER);
    }

    @Test
    @DisplayName("Ability cannot be activated without the mana")
    void requiresMana() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertNotOnBattlefield(player1, "Kelp");
        assertThat(wall.isTapped()).isFalse();
    }

    @Test
    void requiresBlueMana() {
        addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresUntappedWall() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        wall.tap();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
    }

    @Test
    void defenderPreventsAttacking() {
        addCreatureReady(player1, new WallOfKelp());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }

    @Test
    void summoningSicknessPreventsActivation() {
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(wall.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Kelp");
    }

    @Test
    void activationPaysCostsImmediatelyAndCreatesTokenOnlyOnResolution() {
        Permanent wall = addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(wall.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Kelp");

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kelp")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Kelp");
        assertThat(findPermanent(player1, "Kelp").isTapped()).isFalse();
    }

    @Test
    void opponentCanActivateDuringYourTurnAndReceivesToken() {
        addCreatureReady(player2, new WallOfKelp());
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Kelp")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Kelp");
    }

    @Test
    void kelpDefenderPreventsAttackingEvenAfterSummoningSicknessEnds() {
        addCreatureReady(player1, new WallOfKelp());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        findPermanent(player1, "Kelp").setSummoningSick(false);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThat(harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player1.getId())).isEmpty();
    }
}
