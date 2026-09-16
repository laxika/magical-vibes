package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BucketList.class)
class BucketListTest extends BaseCardTest {

    @Test
    void castingTheFirstSpellOfATypeMarksItAndDraws() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());

        castAndResolve(spell("Artifact Spell", CardType.ARTIFACT));

        assertThat(bucketList.getCounterCount(CounterType.ARTIFACT)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void castingTheSameTypeAgainDoesNotAddAnotherMarkerOrDraw() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());

        castAndResolve(spell("First Artifact Spell", CardType.ARTIFACT));
        castAndResolve(spell("Second Artifact Spell", CardType.ARTIFACT));

        assertThat(bucketList.getCounterCount(CounterType.ARTIFACT)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void completingAllFiveTypesSacrificesBucketListAndDrawsAgain() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());

        castAndResolve(spell("Artifact Spell", CardType.ARTIFACT));
        castAndResolve(spell("Creature Spell", CardType.CREATURE));
        castAndResolve(spell("Enchantment Spell", CardType.ENCHANTMENT));
        castAndResolve(spell("Instant Spell", CardType.INSTANT));
        castAndResolve(spell("Sorcery Spell", CardType.SORCERY));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bucketList);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bucketList.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void castAndResolve(Card spell) {
        harness.setHand(player1, List.of(spell));
        switch (spell.getType()) {
            case ARTIFACT -> harness.castArtifact(player1, 0);
            case CREATURE -> harness.castCreature(player1, 0);
            case ENCHANTMENT -> harness.castEnchantment(player1, 0);
            case INSTANT -> harness.castInstant(player1, 0);
            case SORCERY -> harness.castSorcery(player1, 0);
            default -> throw new IllegalArgumentException("Unsupported test spell type: " + spell.getType());
        }
        resolveAllTriggers();
    }

    private static Card spell(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{0}");
        return card;
    }
}
