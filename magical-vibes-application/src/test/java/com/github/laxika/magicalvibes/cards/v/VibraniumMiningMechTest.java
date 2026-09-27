package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VibraniumMiningMech.class, GrizzlyBears.class})
class VibraniumMiningMechTest extends BaseCardTest {

    @Test
    void entersWithTappedIndestructibleVibraniumToken() {
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vibranium = findPermanent(player1, "Vibranium");
        assertThat(vibranium.isTapped()).isTrue();
        assertThat(vibranium.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.hasKeyword(gd, vibranium, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void attackingCreatesAnotherVibraniumToken() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        mech.setSummoningSick(false);
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mech), 1, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mech)));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Vibranium")).hasSize(1);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void activatedAbilityBoostsMechUntilEndOfTurn() {
        Permanent mech = harness.addToBattlefieldAndReturn(player1, new VibraniumMiningMech());
        mech.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mech), 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(7);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, mech)).isEqualTo(6);
    }

    @Test
    void vibraniumManaAbilityProducesPowerstoneMana() {
        harness.setHand(player1, List.of(new VibraniumMiningMech()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Vibranium");
        token.untap();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(token),
                0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(1);
    }
}
