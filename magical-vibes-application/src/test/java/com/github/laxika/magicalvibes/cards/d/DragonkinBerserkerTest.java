package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GoldspanDragon;
import com.github.laxika.magicalvibes.cards.v.VarragothBloodskySire;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonkinBerserker.class, GoldspanDragon.class, VarragothBloodskySire.class, DoomskarOracle.class})
class DragonkinBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Boast creates a 5/5 red Dragon token with flying")
    void boastCreatesDragonToken() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dragon");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Each controlled Dragon reduces the boast activation cost")
    void eachDragonReducesBoastCost() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        addCreatureReady(player1, new GoldspanDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Boast cannot be activated before Dragonkin Berserker attacks")
    void boastRequiresAttack() {
        addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Without Dragons the full boast cost must be paid")
    void noDragonsRequiresFullCost() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Opponent-controlled Dragons do not reduce the boast cost")
    void opponentsDragonsDoNotReduceCost() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        berserker.setAttackedThisTurn(true);
        addCreatureReady(player2, new GoldspanDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Dragon")).isZero();
    }

    @Test
    @DisplayName("Multiple Berserkers' reductions add together")
    void multipleBerserkersStackReductions() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The discount also applies to another creature's boast ability")
    void reducesOtherCreaturesBoastCosts() {
        addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new GoldspanDragon());
        Permanent varragoth = addCreatureReady(player1, new VarragothBloodskySire());
        varragoth.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 2, null, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Even excess Dragons cannot reduce the red mana requirement")
    void reductionCannotRemoveColoredCost() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        berserker.setAttackedThisTurn(true);
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new GoldspanDragon());
        }
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("First strike kills the blocker before it deals damage and boast remains available after combat")
    void firstStrikeAndPostCombatBoast() {
        addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player2, new DoomskarOracle());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Dragonkin Berserker");
        harness.assertInGraveyard(player2, "Doomskar Oracle");
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Dragon")).isEqualTo(1);
    }
}
