package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EusocialEngineering.class, Forest.class})
class EusocialEngineeringTest extends BaseCardTest {

    @Test
    void landfallCreatesAColorlessRobotArtifactToken() {
        harness.addToBattlefield(player1, new EusocialEngineering());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Robot"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ROBOT);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void warpCastsForAlternateCostAndExilesAtNextEndStep() {
        EusocialEngineering enchantment = new EusocialEngineering();
        harness.setHand(player1, List.of(enchantment));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eusocial Engineering");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(enchantment.getId())).isNotNull();
    }

    @Test
    void opponentsLandDoesNotCreateRobot() {
        harness.addToBattlefield(player1, new EusocialEngineering());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void normalCastRemainsOnBattlefieldAtEndStep() {
        EusocialEngineering enchantment = new EusocialEngineering();
        harness.setHand(player1, List.of(enchantment));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eusocial Engineering");
        assertThat(gd.findExiledCard(enchantment.getId())).isNull();
    }

    @Test
    void warpedEnchantmentCreatesRobotAndCanBeCastNormallyOnLaterTurn() {
        EusocialEngineering enchantment = new EusocialEngineering();
        harness.setHand(player1, List.of(enchantment, new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(enchantment.getId())).isNotNull();
        harness.assertNotOnBattlefield(player1, "Eusocial Engineering");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, enchantment.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eusocial Engineering");
        assertThat(gd.findExiledCard(enchantment.getId())).isNull();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Eusocial Engineering");
    }
}
