package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleScreech.class, BishopsSoldier.class})
class BattleScreechTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Battle Screech creates two 1/1 white Bird tokens with flying")
    void createsTwoBirdTokens() {
        harness.setHand(player1, List.of(new BattleScreech()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(birdTokens()).hasSize(2);
        assertThat(birdTokens()).allSatisfy(bird -> {
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(bird.getCard().getPower()).isEqualTo(1);
            assertThat(bird.getCard().getToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Flashback taps three untapped white creatures and creates two more Birds")
    void flashbackTapsThreeWhiteCreaturesAndCreatesBirds() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new BishopsSoldier());
        Card spell = new BattleScreech();
        harness.setGraveyard(player1, List.of(spell));

        harness.castFlashbackWithTapCost(player1, 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(birdTokens()).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private List<Permanent> birdTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Bird"))
                .toList();
    }
}
