package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.b.BeseechTheQueen;
import com.github.laxika.magicalvibes.cards.b.BlindObedience;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.z.ZofShade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrrikSonOfYawgmoth.class, DarkRitual.class, ZofShade.class,
        BeseechTheQueen.class, BlindObedience.class, SolRing.class})
class KrrikSonOfYawgmothTest extends BaseCardTest {

    private Permanent addKrrik() {
        Permanent krrik = harness.addToBattlefieldAndReturn(player1, new KrrikSonOfYawgmoth());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return krrik;
    }

    @Test
    void mayPayTwoLifeForEachBlackManaInAspellCost() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new DarkRitual()));

        harness.castInstant(player1, 0);

        harness.assertLife(player1, 18);
        resolveAllTriggers();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void blackManaIsUsedBeforePayingLife() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        harness.assertLife(player1, 20);
        resolveAllTriggers();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void mayPayLifeForBlackActivatedAbilityCosts() {
        addKrrik();
        Permanent shade = harness.addToBattlefieldAndReturn(player1, new ZofShade());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 1, null, null);

        harness.assertLife(player1, 18);
        harness.passBothPriorities();

        assertThat(shade.getPowerModifier()).isEqualTo(2);
        assertThat(shade.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    void counterTriggerResolvesBeforeBlackSpell() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new DarkRitual()));

        harness.castInstant(player1, 0);

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Dark Ritual");
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "Dark Ritual");
    }

    @Test
    void opponentsBlackSpellDoesNotTriggerKrrik() {
        Permanent krrik = addKrrik();
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castInstant(player2, 0);
        resolveAllTriggers();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void colorlessSpellDoesNotTriggerKrrikEvenWhenPaidWithBlackMana() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        resolveAllTriggers();

        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 20);
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void cannotPayLifeForGenericMana() {
        addKrrik();
        harness.setHand(player1, List.of(new SolRing()));

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Sol Ring");
    }

    @Test
    void cannotPayMoreLifeThanAvailableForBlackSpellCost() {
        addKrrik();
        harness.setLife(player1, 1);
        harness.setHand(player1, List.of(new DarkRitual()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
        harness.assertInHand(player1, "Dark Ritual");
    }

    @Test
    void cannotPayMoreLifeThanAvailableForBlackActivationCost() {
        addKrrik();
        harness.addToBattlefield(player1, new ZofShade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 1);
    }

    @Test
    void mayPayLifeForMonocoloredHybridBlackSymbols() {
        Permanent krrik = addKrrik();
        harness.setHand(player1, List.of(new BeseechTheQueen()));

        harness.castSorcery(player1, 0);

        harness.assertLife(player1, 14);
        harness.passBothPriorities();
        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void mayPayLifeForExtortWhileItsTriggerResolves() {
        addKrrik();
        harness.addToBattlefield(player1, new BlindObedience());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Sol Ring");
    }

    @Test
    void activatedAbilityDoesNotTriggerBlackSpellCounter() {
        Permanent krrik = addKrrik();
        harness.addToBattlefield(player1, new ZofShade());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(krrik.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        Permanent krrik = addKrrik();
        krrik.setSummoningSick(false);
        harness.setLife(player1, 10);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    void castingKrrikDoesNotTriggerItsOwnAbility() {
        harness.setHand(player1, List.of(new KrrikSonOfYawgmoth()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
        assertThat(findPermanent(player1, "K'rrik, Son of Yawgmoth")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
