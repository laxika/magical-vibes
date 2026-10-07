package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.b.BrimstoneMage;
import com.github.laxika.magicalvibes.cards.c.CircleOfProtectionRed;
import com.github.laxika.magicalvibes.cards.d.DawnglareInvoker;
import com.github.laxika.magicalvibes.cards.d.DranaKalastriaBloodchief;
import com.github.laxika.magicalvibes.cards.e.EmberHauler;
import com.github.laxika.magicalvibes.cards.o.OvergrownBattlement;
import com.github.laxika.magicalvibes.cards.s.SpawnsireOfUlamog;
import com.github.laxika.magicalvibes.cards.s.SuppressionField;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainingGrounds.class, AmaranthineWall.class, CircleOfProtectionRed.class, EmberHauler.class,
        DawnglareInvoker.class, DranaKalastriaBloodchief.class, OvergrownBattlement.class, SpawnsireOfUlamog.class,
        BrimstoneMage.class, SuppressionField.class})
class TrainingGroundsTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces a creature's activated ability by two generic mana for its controller")
    void reducesOwnCreatureAbility() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not reduce a creature's ability controlled by an opponent")
    void doesNotReduceOpponentCreatureAbility() {
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addToBattlefield(player2, new TrainingGrounds());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce an activated ability of a noncreature")
    void doesNotReduceNoncreatureAbility() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new CircleOfProtectionRed());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Does not reduce a creature ability below one mana")
    void doesNotReduceBelowOneMana() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new EmberHauler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void reducesEightManaAbilityByExactlyTwo() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new DawnglareInvoker());
        Permanent target = addCreatureReady(player2, new OvergrownBattlement());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 1, null, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void multipleCopiesStackButStillRequireOneMana() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new TrainingGrounds());
        harness.addToBattlefield(player1, new SpawnsireOfUlamog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Eldrazi Spawn")).isEqualTo(2);
    }

    @Test
    void reducesChosenXWithoutChangingItsEffect() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpawnsireOfUlamog());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, 5, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void removesAllGenericManaWhenColoredManaRemains() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 2, drana.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, drana)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, drana)).isEqualTo(2);
    }

    @Test
    void doesNotReplaceRequiredColoredManaWithGenericMana() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        Permanent drana = harness.addToBattlefieldAndReturn(player1, new DranaKalastriaBloodchief());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, drana.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void doesNotAddManaCostToTapOnlyManaAbility() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        Permanent battlement = addCreatureReady(player1, new OvergrownBattlement());

        harness.activateAbility(player1, 1, null, null);

        assertThat(battlement.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void reducesManaTaxOnAbilityWithNoPrintedManaCost() {
        harness.addToBattlefield(player1, new TrainingGrounds());
        Permanent mage = addCreatureReady(player1, new BrimstoneMage());
        mage.setCounterCount(CounterType.LEVEL, 1);
        harness.addToBattlefield(player2, new SuppressionField());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 1, null, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(mage.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
