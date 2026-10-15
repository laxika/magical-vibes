package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AngrathTheFlameChained;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TilonallisSummoner.class, Mountain.class, AngrathTheFlameChained.class})
class TilonallisSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {X}{R} creates X tapped and attacking Elemental tokens")
    void payingCreatesTappedAttackingElementals() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.XValueChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.XValueChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxValue()).isEqualTo(2);

        harness.handleXValueChosen(player1, 2);

        List<Permanent> tokens = findPermanents(player1, "Elemental");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(Permanent::isTapped);
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Created Elementals are exiled at the next end step without the city's blessing")
    void createdElementalsAreExiledAtNextEndStepWithoutBlessing() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isZero();
    }

    @Test
    @DisplayName("Created Elementals remain at the next end step with the city's blessing")
    void createdElementalsRemainAtNextEndStepWithBlessing() {
        addCreatureReady(player1, new TilonallisSummoner());
        gd.playersWithCityBlessing.add(player1.getId());
        harness.addMana(player1, ManaColor.RED, 2);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void decliningPaymentCreatesNoTokensAndSpendsNoMana() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleXValueChosen(player1, 0));

        assertThat(countPermanents(player1, "Elemental")).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    void createdTokenGrantsBlessingAndRemainsAtEndStep() {
        addCreatureReady(player1, new TilonallisSummoner());
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.addMana(player1, ManaColor.RED, 2);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void blessingGainedAfterEndStepTriggerPreventsExile() {
        addCreatureReady(player1, new TilonallisSummoner());
        harness.addMana(player1, ManaColor.RED, 2);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        for (int i = 0; i < 8; i++) {
            harness.enterBattlefieldAndReturn(player1, new Mountain());
        }
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
    }

    @Test
    void eachTokenCanAttackADifferentDefenderThanSummoner() {
        Permanent summoner = addCreatureReady(player1, new TilonallisSummoner());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new AngrathTheFlameChained());
        harness.addMana(player1, ManaColor.RED, 3);
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.handleXValueChosen(player1, 2));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, planeswalker.getId());
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> harness.handlePermanentChosen(player1, player2.getId()));

        assertThat(summoner.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(findPermanents(player1, "Elemental")).hasSize(2)
                .allMatch(Permanent::isTapped)
                .allMatch(Permanent::isAttacking)
                .anySatisfy(token -> assertThat(token.getAttackTarget()).isEqualTo(planeswalker.getId()))
                .anySatisfy(token -> assertThat(token.getAttackTarget()).isEqualTo(player2.getId()));
    }
}
