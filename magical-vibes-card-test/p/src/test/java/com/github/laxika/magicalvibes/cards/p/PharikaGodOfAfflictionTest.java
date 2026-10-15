package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ConsignToDust;
import com.github.laxika.magicalvibes.cards.f.FontOfFertility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PharikaGodOfAffliction.class, PharikasChosen.class, FontOfFertility.class, ConsignToDust.class})
class PharikaGodOfAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("Pharika is not a creature below seven devotion to black and green")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(4);

        assertThat(gqs.isCreature(gd, pharika)).isFalse();
    }

    @Test
    @DisplayName("Pharika becomes a creature at seven devotion to black and green")
    void becomesCreatureAtDevotionThreshold() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(5);

        assertThat(gqs.isCreature(gd, pharika)).isTrue();
    }

    @Test
    @DisplayName("Pharika exiles a creature card and its owner creates a Snake token")
    void exilesCreatureAndOwnerCreatesSnakeToken() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        Card creature = new PharikasChosen();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Pharika's Chosen");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        assertThat(countPermanents(player2, "Snake")).isEqualTo(1);
        Permanent snake = findPermanent(player2, "Snake");
        assertThat(snake.getCard().getPower()).isEqualTo(1);
        assertThat(snake.getCard().getToughness()).isEqualTo(1);
        assertThat(snake.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
        assertThat(snake.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(snake.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(snake.getCard().hasType(CardType.ENCHANTMENT)).isTrue();
    }

    @Test
    @DisplayName("Pharika cannot target a noncreature card in a graveyard")
    void rejectsNonCreatureGraveyardTarget() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        Card enchantment = new FontOfFertility();
        harness.setGraveyard(player2, List.of(enchantment));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pharika creates no token if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        Card creature = new PharikasChosen();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(creature.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Both colors contribute to devotion and losing a permanent turns Pharika back into an enchantment")
    void mixedDevotionUpdatesWhenPermanentLeaves() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(4);
        Permanent font = harness.addToBattlefieldAndReturn(player1, new FontOfFertility());

        assertThat(gqs.isCreature(gd, pharika)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(font);
        assertThat(gqs.isCreature(gd, pharika)).isFalse();
        assertThat(gqs.getEffectiveCardTypes(gd, pharika)).contains(CardType.ENCHANTMENT);
    }

    @Test
    @DisplayName("An opponent's permanents do not contribute to Pharika's devotion")
    void ignoresOpponentsDevotion() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(4);
        harness.addToBattlefield(player2, new PharikasChosen());
        harness.addToBattlefield(player2, new FontOfFertility());

        assertThat(gqs.isCreature(gd, pharika)).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 5})
    @DisplayName("Pharika resists destruction both as an enchantment and as a creature")
    void indestructibleRegardlessOfDevotion(int blackPermanents) {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(blackPermanents);
        harness.setHand(player1, List.of(new ConsignToDust()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.of(pharika.getId()));

        harness.assertOnBattlefield(player1, "Pharika, God of Affliction");
        harness.assertInGraveyard(player1, "Consign to Dust");
    }

    @Test
    @DisplayName("Pharika can exile its controller's creature card even after Pharika leaves the battlefield")
    void ownGraveyardTargetResolvesAfterSourceLeaves() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        Card creature = new PharikasChosen();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(pharika);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Pharika's Chosen");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(countPermanents(player2, "Snake")).isZero();
    }

    @Test
    @DisplayName("A black and green Snake token adds no devotion without a mana cost")
    void snakeTokenDoesNotIncreaseDevotion() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        addBlackPermanents(4);
        Card creature = new PharikasChosen();
        harness.setGraveyard(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Snake")).isEqualTo(1);
        assertThat(gqs.isCreature(gd, pharika)).isFalse();
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new PharikasChosen());
        }
    }
}
