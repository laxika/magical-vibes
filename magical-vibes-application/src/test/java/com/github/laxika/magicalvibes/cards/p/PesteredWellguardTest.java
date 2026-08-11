package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PesteredWellguardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pestered Wellguard creates a blue and black Faerie token")
    void tappingSourceCreatesFaerieToken() {
        Permanent wellguard = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());

        tap(wellguard);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        assertThat(token.getCard().getName()).isEqualTo("Faerie");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Tapping another creature does not trigger Pestered Wellguard")
    void tappingAnotherCreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new PesteredWellguard());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new CoralMerfolk());

        tap(other);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
