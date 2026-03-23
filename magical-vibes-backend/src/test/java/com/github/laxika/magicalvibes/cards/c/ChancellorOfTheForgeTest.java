package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensEqualToControlledCreatureCountEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChancellorOfTheForgeTest extends BaseCardTest {

    @Test
    @DisplayName("Has opening hand reveal effect and ETB token creation effect")
    void hasCorrectEffects() {
        ChancellorOfTheForge card = new ChancellorOfTheForge();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);

        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(CreateTokenEffect.class);

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CreateTokensEqualToControlledCreatureCountEffect.class);
    }

    @Test
    @DisplayName("ETB creates tokens equal to the number of creatures you control (Chancellor + 2 others = 3 tokens)")
    void etbCreatesTokensEqualToCreatureCount() {
        // Put two creatures on the battlefield first
        harness.addToBattlefield(player1, new YouthfulKnight());
        harness.addToBattlefield(player1, new YouthfulKnight());

        harness.setHand(player1, List.of(new ChancellorOfTheForge()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        // 2 Youthful Knights + Chancellor + 3 Goblin tokens (Chancellor counts itself)
        assertThat(battlefield).hasSize(6);

        List<Permanent> goblinTokens = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Goblin"))
                .toList();
        assertThat(goblinTokens).hasSize(3);
    }

    @Test
    @DisplayName("ETB with no other creatures creates 1 token (Chancellor itself)")
    void etbWithNoOtherCreaturesCreatesOneToken() {
        harness.setHand(player1, List.of(new ChancellorOfTheForge()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        // Chancellor + 1 Goblin token (Chancellor counts itself)
        assertThat(battlefield).hasSize(2);

        Permanent goblinToken = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Goblin"))
                .findFirst()
                .orElseThrow();
        assertThat(goblinToken.getEffectivePower()).isEqualTo(1);
        assertThat(goblinToken.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin tokens are 1/1 red Phyrexian Goblins with haste")
    void goblinTokensHaveCorrectCharacteristics() {
        harness.setHand(player1, List.of(new ChancellorOfTheForge()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        Permanent goblinToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Phyrexian Goblin"))
                .findFirst()
                .orElseThrow();

        assertThat(goblinToken.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(goblinToken.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(goblinToken.getCard().getSubtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
        assertThat(goblinToken.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(goblinToken.getCard().isToken()).isTrue();
        assertThat(goblinToken.getEffectivePower()).isEqualTo(1);
        assertThat(goblinToken.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not count opponent's creatures")
    void etbDoesNotCountOpponentCreatures() {
        harness.addToBattlefield(player2, new YouthfulKnight());
        harness.addToBattlefield(player2, new YouthfulKnight());
        harness.addToBattlefield(player2, new YouthfulKnight());

        harness.setHand(player1, List.of(new ChancellorOfTheForge()));
        harness.addMana(player1, ManaColor.RED, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        // Chancellor + 1 Goblin token (only counts Chancellor itself, not opponent's creatures)
        assertThat(battlefield).hasSize(2);
    }

    @Test
    @DisplayName("Opening hand reveal effect wraps a single Goblin token creation")
    void openingHandRevealEffectIsCorrect() {
        MayEffect mayEffect =
                (MayEffect) new ChancellorOfTheForge().getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        CreateTokenEffect revealEffect = (CreateTokenEffect) mayEffect.wrapped();

        assertThat(revealEffect.amount()).isEqualTo(1);
        assertThat(revealEffect.tokenName()).isEqualTo("Phyrexian Goblin");
        assertThat(revealEffect.power()).isEqualTo(1);
        assertThat(revealEffect.toughness()).isEqualTo(1);
        assertThat(revealEffect.color()).isEqualTo(CardColor.RED);
        assertThat(revealEffect.subtypes()).contains(CardSubtype.PHYREXIAN, CardSubtype.GOBLIN);
        assertThat(revealEffect.keywords()).contains(Keyword.HASTE);
    }
}
