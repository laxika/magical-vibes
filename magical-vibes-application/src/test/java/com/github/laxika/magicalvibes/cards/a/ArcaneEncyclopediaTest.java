package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArcaneEncyclopediaTest extends BaseCardTest {

    @Test
    @DisplayName("{3}, {T} draws a card")
    void drawsACard() {
        Permanent encyclopedia = addEncyclopedia();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(encyclopedia.isTapped()).isTrue();
    }

    private Permanent addEncyclopedia() {
        Permanent encyclopedia = new Permanent(new ArcaneEncyclopedia());
        encyclopedia.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(encyclopedia);
        return encyclopedia;
    }
}
