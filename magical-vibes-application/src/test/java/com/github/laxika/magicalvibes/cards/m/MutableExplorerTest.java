package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MutableExplorerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one tapped Mutavault token")
    void etbCreatesTappedMutavaultToken() {
        Permanent token = castAndGetToken();

        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getName()).isEqualTo("Mutavault");
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);
        assertThat(token.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mutavault token taps for one colorless mana")
    void mutavaultTokenTapsForColorlessMana() {
        Permanent token = castAndGetToken();
        token.untap();

        gs.tapPermanent(gd, player1, gd.playerBattlefields.get(player1.getId()).indexOf(token));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mutavault token can become a 2/2 creature with all creature types")
    void mutavaultTokenCanAnimate() {
        Permanent token = castAndGetToken();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);
        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, token, Keyword.CHANGELING)).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.LAND);
    }

    private Permanent castAndGetToken() {
        harness.setHand(player1, List.of(new MutableExplorer()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }
}
