package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarneusCalgar.class, Forest.class})
class MarneusCalgarTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter Master creates two vigilant Astartes Warrior tokens and draws once")
    void createsTokensAndDrawsOnceForTheBatch() {
        Permanent marneus = harness.addToBattlefieldAndReturn(player1, new MarneusCalgar());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(marneus), null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Astartes Warrior");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allMatch(token -> gqs.hasKeyword(gd, token, Keyword.VIGILANCE));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }
}
