package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());

        Permanent snake = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
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
        Card plains = new Plains();
        harness.setGraveyard(player2, List.of(plains));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index, 0, List.of(plains.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Pharika creates no token if the target leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Permanent pharika = harness.addToBattlefieldAndReturn(player1, new PharikaGodOfAffliction());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(pharika);
        harness.activateAbilityWithGraveyardTargets(player1, index, 0, List.of(bears.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void addBlackPermanents(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }
}
