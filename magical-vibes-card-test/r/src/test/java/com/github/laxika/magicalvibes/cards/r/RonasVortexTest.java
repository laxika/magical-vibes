package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.i.Impulse;
import com.github.laxika.magicalvibes.cards.k.KarnLivingLegacy;
import com.github.laxika.magicalvibes.cards.m.MoltenMonstrosity;
import com.github.laxika.magicalvibes.cards.t.TimelyInterference;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonasVortex.class, MoltenMonstrosity.class, KarnLivingLegacy.class,
        Impulse.class, TimelyInterference.class, RelicOfLegends.class})
class RonasVortexTest extends BaseCardTest {

    @Test
    void returnsOpponentsCreatureWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        prepareSpell(false);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.assertInHand(player2, "Molten Monstrosity");
    }

    @Test
    void putsKickedOpponentsCreatureOnBottomOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        harness.setLibrary(player2, List.of(new Impulse(), new TimelyInterference()));
        prepareSpell(true);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Impulse", "Timely Interference", "Molten Monstrosity");
        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
    }

    @Test
    void putsKickedOpponentsPlaneswalkerOnBottomOfOwnersLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KarnLivingLegacy());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setLibrary(player2, List.of(new TimelyInterference()));
        prepareSpell(true);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Timely Interference", "Karn, Living Legacy");
        harness.assertNotOnBattlefield(player2, "Karn, Living Legacy");
    }

    @Test
    void cannotTargetPermanentControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoltenMonstrosity());
        prepareSpell(false);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or planeswalker an opponent controls");
    }

    @Test
    void returnsOpponentsPlaneswalkerWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KarnLivingLegacy());
        target.setCounterCount(CounterType.LOYALTY, 4);
        prepareSpell(false);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Karn, Living Legacy");
        harness.assertInHand(player2, "Karn, Living Legacy");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cannotTargetNoncreatureNonplaneswalker(boolean kicked) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new RelicOfLegends());
        prepareSpell(kicked);

        assertThatThrownBy(() -> castAt(target, kicked))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOwnPlaneswalkerEvenWhenKicked() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new KarnLivingLegacy());
        target.setCounterCount(CounterType.LOYALTY, 4);
        prepareSpell(true);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void sendsOwnedCreatureControlledByOpponentToOwnersZone(boolean kicked) {
        MoltenMonstrosity creature = new MoltenMonstrosity();
        creature.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player1, List.of(new Impulse()));
        harness.setLibrary(player2, List.of(new TimelyInterference()));
        prepareSpell(kicked);

        castAt(target, kicked);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.assertNotInHand(player2, "Molten Monstrosity");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Timely Interference");
        if (kicked) {
            harness.assertNotInHand(player1, "Molten Monstrosity");
            assertThat(gd.playerDecks.get(player1.getId()))
                    .extracting(Card::getName).containsExactly("Impulse", "Molten Monstrosity");
        } else {
            harness.assertInHand(player1, "Molten Monstrosity");
            assertThat(gd.playerDecks.get(player1.getId()))
                    .extracting(Card::getName).containsExactly("Impulse");
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void doesNotResolveWhenCasterGainsControlOfTarget(boolean kicked) {
        MoltenMonstrosity creature = new MoltenMonstrosity();
        creature.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, creature);
        harness.setLibrary(player2, List.of(new Impulse()));
        prepareSpell(kicked);

        castAt(target, kicked);
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molten Monstrosity");
        harness.assertNotInHand(player2, "Molten Monstrosity");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Impulse");
        harness.assertInGraveyard(player1, "Rona's Vortex");
    }

    @Test
    void kickedCreatureBecomesOnlyCardInEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        harness.setLibrary(player2, List.of());
        prepareSpell(true);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Molten Monstrosity");
        harness.assertNotInHand(player2, "Molten Monstrosity");
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName).containsExactly("Molten Monstrosity");
    }

    @Test
    void cannotKickWithoutAdditionalMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoltenMonstrosity());
        prepareSpell(false);

        assertThatThrownBy(() -> harness.castKickedInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAt(Permanent target, boolean kicked) {
        if (kicked) {
            harness.castKickedInstant(player1, 0, target.getId());
        } else {
            harness.castInstant(player1, 0, target.getId());
        }
    }

    private void prepareSpell(boolean kicked) {
        harness.setHand(player1, List.of(new RonasVortex()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
        }
    }
}
