package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PesteredWellguard.class, CoralMerfolk.class})
class PesteredWellguardTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pestered Wellguard creates a blue and black Faerie token")
    void tappingSourceCreatesFaerieToken() {
        Permanent wellguard = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());

        tap(wellguard);
        resolveAllTriggers();

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
        assertThat(token.isTapped()).isFalse();
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
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

    @Test
    @DisplayName("Attacking creates a Faerie token for the attacking Wellguard's controller")
    void attackingCreatesFaerieToken() {
        Permanent wellguard = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());
        wellguard.setSummoningSick(false);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(wellguard.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);
        assertThat(countPermanents(player2, "Faerie")).isZero();
    }

    @Test
    @DisplayName("Each Wellguard triggers only for itself")
    void multipleWellguardsDoNotTriggerForEachOther() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());

        tap(first);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(1);

        tap(second);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Faerie")).isEqualTo(2);
    }

    @Test
    @DisplayName("Untapping and tapping again creates another token in the same turn")
    void repeatedTapEventsCreateSeparateTokens() {
        Permanent wellguard = harness.addToBattlefieldAndReturn(player1, new PesteredWellguard());

        tap(wellguard);
        resolveAllTriggers();
        wellguard.untap();
        tap(wellguard);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Faerie")).isEqualTo(2);
    }

    @Test
    @DisplayName("A queued token trigger resolves after Wellguard leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent wellguard = harness.addToBattlefieldAndReturn(player2, new PesteredWellguard());

        tap(wellguard);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(wellguard);
        gd.playerGraveyards.get(player2.getId()).add(wellguard.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Faerie")).isEqualTo(1);
        assertThat(countPermanents(player1, "Faerie")).isZero();
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
