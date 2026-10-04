package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CivilServant;
import com.github.laxika.magicalvibes.cards.m.MajesticMetamorphosis;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HaloFountain.class, CivilServant.class, Forest.class, MajesticMetamorphosis.class})
class HaloFountainTest extends BaseCardTest {

    @Test
    void untapsCreatureAndCreatesCitizenToken() {
        Permanent fountain = addFountain();
        Permanent creature = addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void untapsTwoCreaturesAndDrawsCard() {
        Permanent fountain = addFountain();
        Permanent firstCreature = addTappedCreature(player1);
        Permanent secondCreature = addTappedCreature(player1);
        Forest forest = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isFalse();
        assertThat(secondCreature.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
    }

    @Test
    void untapsFifteenCreaturesAndWinsTheGame() {
        addFountain();
        List<Permanent> creatures = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            creatures.add(addTappedCreature(player1));
        }
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(creatures).allSatisfy(creature -> assertThat(creature.isTapped()).isFalse());
        assertThat(gd.gameResult).isEqualTo(GameEventFact.GameResult.WIN);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void cannotActivateWithoutEnoughTappedCreatures() {
        addFountain();
        addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void paysUntapCostBeforeTokenCreationResolves() {
        addFountain();
        Permanent creature = addTappedCreature(player1);
        creature.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(creature.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Citizen")).isEmpty();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
        Permanent token = findPermanents(player1, "Citizen").getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CITIZEN);
    }

    @Test
    void cannotPayWithAnOpponentsCreature() {
        addFountain();
        addTappedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayWithAnUntappedCreatureOrTappedLand() {
        addFountain();
        addCreatureReady(player1, new CivilServant());
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateAnUnanimatedTappedFountain() {
        addFountain().tap();
        addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotSubstituteGenericManaForWhiteMana() {
        addFountain();
        addTappedCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotWinWithOnlyFourteenTappedCreatures() {
        addFountain();
        for (int i = 0; i < 14; i++) {
            addTappedCreature(player1);
        }
        harness.addMana(player1, ManaColor.WHITE, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canPayUntapCostByRemovingAStunCounter() {
        addFountain();
        Permanent creature = addTappedCreature(player1);
        creature.setCounterCount(CounterType.STUN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getCounterCount(CounterType.STUN)).isZero();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void cannotCountTheSameStunnedCreatureTwiceForDrawCost() {
        addFountain();
        Permanent stunned = addTappedCreature(player1);
        stunned.setCounterCount(CounterType.STUN, 2);
        addTappedCreature(player1);
        addTappedCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, stunned.getId());

        assertThat(stunned.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, stunned.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animatedUntappedFountainCanTapThenUntapItselfToCreateToken() {
        Permanent fountain = addFountain();
        animateFountain(fountain);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isFalse();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    @Test
    void animatedTappedFountainCanUntapThenTapItselfToCreateToken() {
        Permanent fountain = addFountain();
        animateFountain(fountain);
        fountain.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
        assertThat(findPermanents(player1, "Citizen")).hasSize(1);
    }

    private void animateFountain(Permanent fountain) {
        harness.setHand(player1, List.of(new MajesticMetamorphosis()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, fountain.getId());
    }

    private Permanent addFountain() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new HaloFountain());
        fountain.setSummoningSick(false);
        return fountain;
    }

    private Permanent addTappedCreature(Player player) {
        Permanent creature = addCreatureReady(player, new CivilServant());
        creature.tap();
        return creature;
    }
}
