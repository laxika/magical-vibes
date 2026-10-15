package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuryInBooks.class, SpinedKarok.class, Island.class})
class BuryInBooksTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target creature second from the top of its owner's library")
    void putsTargetCreatureSecondFromTop() {
        Permanent attacker = addAttacker(player2, player1, new SpinedKarok());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island(), new Island()));

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, attacker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library.get(0)).isSameAs(topCard);
        assertThat(library.get(1).getName()).isEqualTo("Spined Karok");
    }

    @Test
    @DisplayName("Costs {2}{U} when targeting an attacking creature")
    void costsReducedAmountWhenTargetingAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new SpinedKarok());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, attacker.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Requires the full cost when targeting a non-attacking creature")
    void requiresFullCostWhenTargetingNonAttackingCreature() {
        Permanent attacker = addAttacker(player2, player1, new SpinedKarok());
        Permanent nonAttacker = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Can target a non-attacking creature when paying the full cost")
    void targetsNonAttackingCreatureAtFullCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());

        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
    }

    @Test
    @DisplayName("An empty library receives the creature as its only card")
    void putsCreatureOnTopOfEmptyLibrary() {
        Card creature = new SpinedKarok();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("A one-card library receives the creature below its existing card")
    void putsCreatureBelowOnlyLibraryCard() {
        Card creature = new SpinedKarok();
        Card top = new Island();
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, creature);
    }

    @Test
    @DisplayName("A creature controlled by another player goes to its owner's library")
    void usesOwnersLibraryRatherThanControllers() {
        Card creature = new SpinedKarok();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        Card ownerTop = new Island();
        Card controllerTop = new Island();
        harness.setLibrary(player1, List.of(ownerTop));
        harness.setLibrary(player2, List.of(controllerTop));
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(ownerTop, creature);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(controllerTop);
    }

    @Test
    @DisplayName("Can target the caster's own attacking creature at the reduced cost")
    void canBuryOwnAttackingCreature() {
        Card creature = new SpinedKarok();
        Permanent target = addAttacker(player1, player2, creature);
        Card top = new Island();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void rejectsNoncreatureTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The cost reduction does not remove the blue mana requirement")
    void stillRequiresBlueManaForAttackingTarget() {
        Permanent target = addAttacker(player2, player1, new SpinedKarok());
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature that stops attacking after casting remains a legal target")
    void resolvesAfterTargetStopsAttacking() {
        Card creature = new SpinedKarok();
        Permanent target = addAttacker(player2, player1, creature);
        Card top = new Island();
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(new BuryInBooks()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, target.getId());
        target.setAttacking(false);
        target.setAttackTarget(null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Spined Karok");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, creature);
    }

    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player controller,
                                  com.github.laxika.magicalvibes.model.Player defender,
                                  Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(controller, card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        permanent.setAttackTarget(defender.getId());
        return permanent;
    }
}
