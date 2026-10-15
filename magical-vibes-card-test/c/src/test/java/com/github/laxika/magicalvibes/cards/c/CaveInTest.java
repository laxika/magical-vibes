package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FlailingSoldier;
import com.github.laxika.magicalvibes.cards.g.GerrardsIrregulars;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaveIn.class, CrenellatedWall.class, GerrardsIrregulars.class, FlailingSoldier.class,
        CloudSprite.class})
class CaveInTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each creature and each player")
    void dealsDamageToEachCreatureAndPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new CrenellatedWall());
        harness.addToBattlefield(player2, new GerrardsIrregulars());
        harness.setHand(player1, List.of(new CaveIn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertOnBattlefield(player1, "Crenellated Wall");
        harness.assertNotOnBattlefield(player2, "Gerrard's Irregulars");
        assertThat(wall.getMarkedDamage()).isEqualTo(2);
        GameData game = harness.getGameData();
        assertThat(game.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(game.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can be cast by exiling a red card from hand")
    void castsForAlternateCost() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player2, new CrenellatedWall());
        harness.setHand(player1, List.of(new CaveIn(), new FlailingSoldier()));
        harness.castSorceryWithDiscard(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Crenellated Wall");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(e -> e.card().getName()).containsExactly("Flailing Soldier");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cannot use a non-red card for the alternate cost")
    void rejectsNonRedAlternateCostCard() {
        harness.setHand(player1, List.of(new CaveIn(), new CloudSprite()));
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Exiled card must be red card");

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Cave-In", "Cloud Sprite");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void damagesFlyingCreaturesOnBothSides() {
        harness.addToBattlefield(player1, new CloudSprite());
        harness.addToBattlefield(player2, new CloudSprite());
        harness.setHand(player1, List.of(new CaveIn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertNotOnBattlefield(player1, "Cloud Sprite");
        harness.assertNotOnBattlefield(player2, "Cloud Sprite");
        harness.assertInGraveyard(player1, "Cloud Sprite");
        harness.assertInGraveyard(player2, "Cloud Sprite");
    }

    @Test
    void canExileAnotherCaveInBeforeTheSpellInHand() {
        CaveIn payment = new CaveIn();
        CaveIn spell = new CaveIn();
        harness.setHand(player1, List.of(payment, spell, new CloudSprite()));

        harness.castSorceryWithDiscard(player1, 1, 0);

        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Cloud Sprite");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.exiledCards).extracting(e -> e.card()).containsExactly(payment);
    }

    @Test
    void cannotExileTheSpellItself() {
        harness.setHand(player1, List.of(new CaveIn(), new FlailingSoldier()));

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Cave-In", "Flailing Soldier");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
