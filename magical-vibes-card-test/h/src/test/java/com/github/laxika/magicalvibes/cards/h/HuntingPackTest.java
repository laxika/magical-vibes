package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrainFreeze;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntingPack.class, BrainFreeze.class})
class HuntingPackTest extends BaseCardTest {

    @Test
    @DisplayName("Cast creates a 4/4 green Beast creature token")
    void createsBeastToken() {
        castHuntingPack();
        resolveAllTriggers();

        List<Permanent> tokens = findPermanents(player1, "Beast");
        assertThat(tokens).hasSize(1);
        assertThat(findPermanents(player2, "Beast")).isEmpty();
        Permanent beast = tokens.getFirst();
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Storm creates one copy for each spell cast before Hunting Pack")
    void stormCopiesForEachPriorSpell() {
        gd.recordSpellCast(player1.getId(), new BrainFreeze());
        gd.recordSpellCast(player2.getId(), new BrainFreeze());

        castHuntingPack();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(3);
    }

    @Test
    @DisplayName("Spells cast in response do not increase an earlier storm count")
    void laterSpellDoesNotIncreaseEarlierStormCount() {
        castHuntingPack();
        harness.castFromHand(player2, new HuntingPack(), "{5}{G}{G}");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(1);
        assertThat(findPermanents(player2, "Beast")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm copies are not casts and do not increase a later storm count")
    void copiesDoNotIncreaseLaterStormCount() {
        castHuntingPack();
        resolveAllTriggers();
        castHuntingPack();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(3);

        castHuntingPack();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Beast")).hasSize(6);
        assertThat(findPermanents(player2, "Beast")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void castHuntingPack() {
        harness.castFromHand(player1, new HuntingPack(), "{5}{G}{G}");
    }
}
