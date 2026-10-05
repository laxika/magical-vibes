package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BrazenFreebooter;
import com.github.laxika.magicalvibes.cards.h.HangedExecutioner;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.t.TempleOfCivilization;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjerTaqDeepestFoundation.class, TempleOfCivilization.class, HangedExecutioner.class,
        BrazenFreebooter.class, Murder.class})
class OjerTaqDeepestFoundationTest extends BaseCardTest {

    @Test
    @DisplayName("Triples creature tokens created under its controller's control")
    void triplesCreatureTokens() {
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.castFromHand(player1, new HangedExecutioner(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(3);
    }

    @Test
    @DisplayName("Does not multiply noncreature tokens")
    void doesNotMultiplyNoncreatureTokens() {
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.castFromHand(player1, new BrazenFreebooter(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Returns tapped and transformed when it dies")
    void returnsTappedAndTransformedWhenItDies() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerTaqDeepestFoundation());
        destroyOjer(ojer);

        Permanent temple = findPermanents(player1, "Temple of Civilization").getFirst();
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
        harness.assertNotInGraveyard(player1, "Ojer Taq, Deepest Foundation");
    }

    @Test
    @DisplayName("Transforms back after attacking with three creatures")
    void transformsBackAfterAttackingWithThreeCreatures() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 3);
        addTransformMana();

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null);
        harness.passBothPriorities();

        assertThat(temple.getCard().getName()).isEqualTo("Ojer Taq, Deepest Foundation");
        assertThat(temple.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Cannot transform back without attacking with three creatures")
    void cannotTransformBackWithoutAttackingWithThreeCreatures() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        addTransformMana();

        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);

        assertThat(temple.getCard()).isInstanceOf(TempleOfCivilization.class);
        assertThat(temple.isTapped()).isFalse();
    }

    @Test
    void doesNotMultiplyOpponentsCreatureTokens() {
        harness.addToBattlefield(player1, new OjerTaqDeepestFoundation());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player2, new HangedExecutioner(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    void templeDoesNotMultiplyCreatureTokens() {
        returnOjerAsTemple();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new HangedExecutioner(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void templeProducesWhiteManaWithoutAttacking() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        gd.playerManaPools.get(player1.getId()).clear();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(temple), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(temple.isTapped()).isTrue();
        assertThat(temple.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTransformAfterOnlyTwoAttackers() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 2);
        addTransformMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.isTransformed()).isTrue();
    }

    @Test
    void cannotTransformDuringCombatEvenAfterThreeAttackers() {
        Permanent temple = returnOjerAsTemple();
        temple.untap();
        gd.creaturesAttackedCountThisTurn.put(player1.getId(), 3);
        addTransformMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(temple), 1, null, null))
                .isInstanceOf(RuntimeException.class);
        assertThat(temple.isTapped()).isFalse();
        assertThat(temple.isTransformed()).isTrue();
    }

    @Test
    void stolenOjerReturnsUnderItsOwnersControl() {
        OjerTaqDeepestFoundation card = new OjerTaqDeepestFoundation();
        card.setOwnerId(player1.getId());
        Permanent ojer = harness.addToBattlefieldAndReturn(player2, card);

        destroyOjer(ojer);

        assertThat(findPermanents(player1, "Temple of Civilization")).hasSize(1);
        assertThat(findPermanents(player1, "Temple of Civilization").getFirst().isTapped()).isTrue();
        assertThat(findPermanents(player2, "Temple of Civilization")).isEmpty();
        harness.assertNotInGraveyard(player1, "Ojer Taq, Deepest Foundation");
        harness.assertNotInGraveyard(player2, "Ojer Taq, Deepest Foundation");
    }

    private void destroyOjer(Permanent ojer) {
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, ojer.getId());
        harness.passBothPriorities();
    }

    private Permanent returnOjerAsTemple() {
        Permanent ojer = harness.addToBattlefieldAndReturn(player1, new OjerTaqDeepestFoundation());
        destroyOjer(ojer);
        return findPermanents(player1, "Temple of Civilization").getFirst();
    }

    private void addTransformMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
