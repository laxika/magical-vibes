package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelTheMuscle.class, GrizzlyBears.class, ZuranSpellcaster.class})
class RaphaelTheMuscleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Mutagen token when it enters")
    void createsMutagenToken() {
        harness.setHand(player1, List.of(new RaphaelTheMuscle()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
    }

    @Test
    @DisplayName("Doubles damage from a controlled creature with a counter")
    void doublesDamageFromCreatureWithCounter() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        spellcaster.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(player1, spellcaster), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a controlled creature without counters")
    void doesNotDoubleDamageFromCreatureWithoutCounters() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, battlefieldIndex(player1, spellcaster), null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Doubles combat damage from a creature with a counter")
    void doublesCombatDamageFromCreatureWithCounter() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player2, 20);

        declareAttackers(List.of(battlefieldIndex(player1, attacker)));

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void createsTokenWithMutagenSubtype() {
        harness.enterBattlefieldAndReturn(player1, new RaphaelTheMuscle());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Mutagen").getCard().getSubtypes())
                .contains(CardSubtype.MUTAGEN);
    }

    @Test
    void mutagenCanPutCounterOnOpponentsCreature() {
        harness.enterBattlefieldAndReturn(player1, new RaphaelTheMuscle());
        resolveAllTriggers();
        Permanent target = addCreatureReady(player2, new RaphaelTheMuscle());
        Permanent token = findPermanent(player1, "Mutagen");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, token), null, target.getId());

        assertThat(findPermanents(player1, "Mutagen")).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mutagenCannotBeActivatedDuringCombat() {
        Permanent target = harness.enterBattlefieldAndReturn(player1, new RaphaelTheMuscle());
        resolveAllTriggers();
        Permanent token = findPermanent(player1, "Mutagen");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, token), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Mutagen")).hasSize(1);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotDoubleOpponentsCreatureDamage() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player2, new ZuranSpellcaster());
        spellcaster.setCounterCount(CounterType.CHARGE, 1);
        harness.setLife(player1, 20);

        harness.activateAbility(player2, battlefieldIndex(player2, spellcaster), null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    void doublesDamageToCreaturesOnlyOnceForMultipleCounters() {
        addCreatureReady(player1, new RaphaelTheMuscle());
        Permanent spellcaster = addCreatureReady(player1, new ZuranSpellcaster());
        spellcaster.setCounterCount(CounterType.CHARGE, 3);
        Permanent target = addCreatureReady(player2, new RaphaelTheMuscle());

        harness.activateAbility(player1, battlefieldIndex(player1, spellcaster), null, target.getId());
        resolveAllTriggers();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
