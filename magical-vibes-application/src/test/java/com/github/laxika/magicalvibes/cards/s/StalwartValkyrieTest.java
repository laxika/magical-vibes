package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeholdTheMultiverse;
import com.github.laxika.magicalvibes.cards.g.GiantOx;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StalwartValkyrie.class, BeholdTheMultiverse.class, GiantOx.class})
class StalwartValkyrieTest extends BaseCardTest {

    @Test
    void alternateCostExilesChosenCreatureCardFromGraveyard() {
        StalwartValkyrie valkyrie = new StalwartValkyrie();
        BeholdTheMultiverse noncreature = new BeholdTheMultiverse();
        GiantOx creature = new GiantOx();
        harness.setHand(player1, List.of(valkyrie));
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement()
                .extracting(Permanent::getCard)
                .isSameAs(valkyrie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void alternateCostRequiresACreatureCardAndDoesNotPayManaOnFailure() {
        BeholdTheMultiverse noncreature = new BeholdTheMultiverse();
        harness.setHand(player1, List.of(new StalwartValkyrie()));
        harness.setGraveyard(player1, List.of(noncreature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    void normalCostDoesNotExileACreatureCard() {
        GiantOx creature = new GiantOx();
        harness.setGraveyard(player1, List.of(creature));
        harness.castFromHand(player1, new StalwartValkyrie(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stalwart Valkyrie");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void normalCostCanBePaidWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new StalwartValkyrie(), "{3}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stalwart Valkyrie");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void alternateCostExilesTheSelectedCardBeforeResolution() {
        GiantOx first = new GiantOx();
        GiantOx chosen = new GiantOx();
        harness.setHand(player1, List.of(new StalwartValkyrie()));
        harness.setGraveyard(player1, List.of(first, chosen));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithGraveyardExile(player1, 0, 1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.exiledCards).singleElement()
                .satisfies(entry -> assertThat(entry.card()).isSameAs(chosen));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.assertNotOnBattlefield(player1, "Stalwart Valkyrie");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stalwart Valkyrie");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    void alternateCostCannotUseAnOpponentsGraveyard() {
        GiantOx creature = new GiantOx();
        StalwartValkyrie valkyrie = new StalwartValkyrie();
        harness.setHand(player1, List.of(valkyrie));
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(valkyrie);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @ParameterizedTest
    @CsvSource({"0, 2", "1, 0"})
    void insufficientAlternateManaDoesNotExileOrPayMana(int white, int colorless) {
        GiantOx creature = new GiantOx();
        StalwartValkyrie valkyrie = new StalwartValkyrie();
        harness.setHand(player1, List.of(valkyrie));
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.WHITE, white);
        harness.addMana(player1, ManaColor.COLORLESS, colorless);

        assertThatThrownBy(() -> harness.castCreatureWithGraveyardExile(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(valkyrie);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(white + colorless);
    }

    @Test
    void flyingPreventsGroundBlockersButAllowsFlyingBlockers() {
        harness.castFromHand(player1, new StalwartValkyrie(), "{3}{W}");
        harness.passBothPriorities();

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent groundBlocker = harness.addToBattlefieldAndReturn(player2, new GiantOx());
        Permanent flyingBlocker = harness.addToBattlefieldAndReturn(player2, new StalwartValkyrie());

        assertThat(bls.canBlockAttacker(gd, groundBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }
}
