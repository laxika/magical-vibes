package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.v.ValakutInvoker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastcallerSavant.class, LlanowarElves.class, Divination.class, ValakutInvoker.class})
class BeastcallerSavantTest extends BaseCardTest {

    @Test
    void tappingPromptsForManaColorWithoutUsingTheStack() {
        Permanent savant = addReadySavant();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(savant.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    void chosenColorProducesCreatureSpellOnlyMana() {
        addReadySavant();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void creatureSpellOnlyManaCanCastCreatureSpells() {
        addReadySavant();
        harness.setHand(player1, List.of(new LlanowarElves()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
    }

    @Test
    void creatureSpellOnlyManaCannotCastNoncreatureSpells() {
        addReadySavant();
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void canProduceEachColorOfRestrictedMana(ManaColor color) {
        addReadySavant();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellOnlyMana(color)).isEqualTo(1);
        assertThat(pool.getCreatureSpellOnlyManaTotal()).isEqualTo(1);
        assertThat(pool.get(color)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hasteAllowsTappingOnTheTurnItEnters() {
        Permanent savant = harness.enterBattlefieldAndReturn(player1, new BeastcallerSavant());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(savant.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN))
                .isEqualTo(1);
    }

    @Test
    void hasteAllowsAttackingOnTheTurnItEnters() {
        Permanent savant = harness.enterBattlefieldAndReturn(player1, new BeastcallerSavant());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(savant.isAttacking()).isTrue();
        assertThat(savant.isTapped()).isTrue();
    }

    @Test
    void restrictedManaCanPayGenericCreatureCosts() {
        addReadySavant();
        harness.setHand(player1, List.of(new BeastcallerSavant()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    void restrictedManaCannotPayForCreatureActivatedAbilities() {
        addReadySavant();
        harness.addToBattlefield(player1, new ValakutInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.RED))
                .isEqualTo(1);
    }

    @Test
    void tappedSavantCannotProduceManaAgain() {
        addReadySavant();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyManaTotal()).isEqualTo(1);
    }

    private Permanent addReadySavant() {
        Permanent savant = harness.addToBattlefieldAndReturn(player1, new BeastcallerSavant());
        savant.setSummoningSick(false);
        return savant;
    }
}
