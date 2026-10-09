package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Prismite;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrushDissent.class, Prismite.class})
class CrushDissentTest extends BaseCardTest {

    @Test
    void countersSpellAndAmassesWithoutArmyWhenControllerCannotPay() {
        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        harness.assertInGraveyard(player1, "Prismite");
        Permanent army = findArmyToken();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void countersSpellWhenControllerDeclinesAndStillAmasses() {
        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Prismite");
        Permanent army = findArmyToken();
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void allowsSpellWhenControllerPaysAndAmassesOnExistingArmy() {
        Permanent army = harness.addToBattlefieldAndReturn(player2, new Prismite());
        army.getGrantedSubtypes().add(CardSubtype.ARMY);

        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Prismite");
        assertThat(army.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(army.getGrantedSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    void amassesOnOnlyTheChosenArmyAndAddsZombieSubtype() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new Prismite());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new Prismite());
        first.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getGrantedSubtypes().add(CardSubtype.ARMY);
        second.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 3);

        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        harness.assertInGraveyard(player1, "Prismite");
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(first.getGrantedSubtypes()).doesNotContain(CardSubtype.ZOMBIE);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(second.getGrantedSubtypes()).contains(CardSubtype.ARMY, CardSubtype.ZOMBIE);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
    }

    @Test
    void createsOwnArmyWhenOnlyOpponentControlsAnArmyAndPaymentIsAccepted() {
        Permanent opposingArmy = harness.addToBattlefieldAndReturn(player1, new Prismite());
        opposingArmy.getGrantedSubtypes().add(CardSubtype.ARMY);

        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findArmyToken().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposingArmy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }

    @Test
    void doesNotAmassWhenTargetSpellWasAlreadyCountered() {
        Prismite spell = new Prismite();
        harness.setHand(player1, List.of(spell, new CrushDissent()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.setHand(player2, List.of(new CrushDissent()));
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.castAndResolveInstant(player1, 0, spell.getId());

        harness.assertInGraveyard(player1, "Prismite");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Crush Dissent");
    }
    private Permanent findArmyToken() {
        return gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
