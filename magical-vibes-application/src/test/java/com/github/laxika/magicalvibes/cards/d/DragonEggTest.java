package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DragonEggTest extends BaseCardTest {

    @Test
    @DisplayName("When Dragon Egg dies, a 2/2 red flying Dragon token is created")
    void deathTriggerCreatesDragonToken() {
        harness.addToBattlefield(player1, new DragonEgg());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities(); // Wrath resolves — Dragon Egg dies
        harness.passBothPriorities(); // Death trigger resolves

        List<Permanent> tokens = findPermanents(player1, "Dragon");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("The Dragon token's {R} ability gives it +1/+0 until end of turn")
    void dragonTokenHasFirebreathing() {
        harness.addToBattlefield(player1, new DragonEgg());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(findPermanents(player1, "Dragon").getFirst());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, tokenIndex, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanents(player1, "Dragon").getFirst();
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(token.getEffectivePower()).isEqualTo(2);
    }
}
