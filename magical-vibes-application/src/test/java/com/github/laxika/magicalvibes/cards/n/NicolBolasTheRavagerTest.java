package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NicolBolasTheRavagerTest extends BaseCardTest {

    @Test
    void etbMakesEachOpponentDiscardOneCard() {
        Card discarded = new Shock();
        harness.setHand(player1, List.of(new NicolBolasTheRavager()));
        harness.setHand(player2, List.of(discarded));
        addManaForCreature();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    void sorceryAbilityReturnsNicolBolasTransformed() {
        addReadyRavager();
        addManaForTransform();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCounterCount(CounterType.LOYALTY)).isPositive();
    }

    @Test
    void plusTwoDrawsTwoCards() {
        Permanent bolas = addReadyArisen();
        Card first = new Shock();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));

        harness.activateAbility(player1, indexOf(bolas), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void minusThreeDestroysAPlaneswalkerWithTenDamage() {
        Permanent bolas = addReadyArisen();
        Permanent target = new Permanent(new NicolBolasGodPharaoh());
        target.setCounterCount(CounterType.LOYALTY, 10);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.activateAbility(player1, indexOf(bolas), 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void minusFourReanimatesAPlaneswalkerFromAnyGraveyard() {
        Permanent bolas = addReadyArisen();
        Card target = new NicolBolasGodPharaoh();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbility(player1, indexOf(bolas), 2, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    void minusTwelveExilesAllButTheBottomCardOfTargetLibrary() {
        Permanent bolas = addReadyArisen();
        Card top = new Shock();
        Card middle = new GrizzlyBears();
        Card bottom = new Shock();
        harness.setLibrary(player2, List.of(top, middle, bottom));

        harness.activateAbility(player1, indexOf(bolas), 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(bottom);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(top, middle)
                .doesNotContain(bottom);
    }

    private Permanent addReadyRavager() {
        Permanent permanent = new Permanent(new NicolBolasTheRavager());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        prepareMainPhase();
        return permanent;
    }

    private Permanent addReadyArisen() {
        NicolBolasTheRavager card = new NicolBolasTheRavager();
        Permanent permanent = new Permanent(card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setCounterCount(CounterType.LOYALTY, 20);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        prepareMainPhase();
        return permanent;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void addManaForCreature() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void addManaForTransform() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }
}
