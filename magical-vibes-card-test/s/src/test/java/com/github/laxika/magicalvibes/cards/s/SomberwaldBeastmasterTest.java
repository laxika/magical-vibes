package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CacklingCounterpart;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SomberwaldBeastmaster.class, CacklingCounterpart.class})
class SomberwaldBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 2/2 Wolf, a 3/3 Beast, and a 4/4 Beast")
    void createsThreeDifferentTokens() {
        harness.setHand(player1, List.of(new SomberwaldBeastmaster()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).extracting(permanent -> permanent.getCard().getName())
                .containsExactlyInAnyOrder("Wolf", "Beast", "Beast");
        assertThat(tokens).extracting(permanent -> gqs.getEffectivePower(gd, permanent))
                .containsExactlyInAnyOrder(2, 3, 4);
        assertThat(tokens).extracting(permanent -> gqs.getEffectiveToughness(gd, permanent))
                .containsExactlyInAnyOrder(2, 3, 4);
    }

    @Test
    @DisplayName("Gives deathtouch to creature tokens but not nontoken creatures")
    void creatureTokensHaveDeathtouch() {
        Permanent nontoken = harness.addToBattlefieldAndReturn(player1, new SomberwaldBeastmaster());
        harness.setHand(player1, List.of(new SomberwaldBeastmaster()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue());
        assertThat(gqs.hasKeyword(gd, nontoken, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    @DisplayName("Tokens lose deathtouch when the last Beastmaster leaves")
    void tokensLoseDeathtouchWhenSourceLeaves() {
        Permanent source = harness.enterBattlefieldAndReturn(player1, new SomberwaldBeastmaster());
        resolveAllTriggers();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(3);
        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isTrue());

        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        assertThat(tokens).allSatisfy(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.DEATHTOUCH)).isFalse());
    }

    @Test
    @DisplayName("Only the current controller's creature tokens receive deathtouch")
    void grantFollowsTokenController() {
        harness.enterBattlefieldAndReturn(player1, new SomberwaldBeastmaster());
        resolveAllTriggers();
        Permanent wolf = findPermanent(player1, "Wolf");
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(wolf);
        gd.playerBattlefields.get(player2.getId()).add(wolf);
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isFalse();

        harness.addToBattlefield(player2, new SomberwaldBeastmaster());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A token copy of Beastmaster grants itself deathtouch")
    void tokenCopyGrantsItselfDeathtouch() {
        Permanent original = harness.addToBattlefieldAndReturn(player1, new SomberwaldBeastmaster());
        harness.setHand(player1, List.of(new CacklingCounterpart()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castAndResolveInstant(player1, 0, original.getId());
        resolveAllTriggers();
        Permanent copy = findPermanents(player1, "Somberwald Beastmaster").stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        gd.playerBattlefields.get(player1.getId()).remove(original);
        gd.playerGraveyards.get(player1.getId()).add(original.getCard());

        assertThat(gqs.hasKeyword(gd, copy, Keyword.DEATHTOUCH)).isTrue();
    }
}
