package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.ReturnToTheEarth;
import com.github.laxika.magicalvibes.cards.w.WhispererOfTheWilds;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cloudform.class, WhispererOfTheWilds.class, Forest.class, ReturnToTheEarth.class})
class CloudformTest extends BaseCardTest {

    @Test
    void manifestsTopCardAttachesAndGrantsFlyingAndHexproof() {
        Permanent manifested = resolveCloudform(new WhispererOfTheWilds());
        Permanent cloudform = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isAura())
                .findFirst()
                .orElseThrow();

        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(cloudform.isAttached()).isTrue();
        assertThat(cloudform.getAttachedTo()).isEqualTo(manifested.getId());
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestedCreatureCanTurnFaceUpAndRetainsGrantedKeywords() {
        Permanent manifested = resolveCloudform(new WhispererOfTheWilds());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.isManifested()).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    void manifestedNoncreatureCannotTurnFaceUp() {
        Permanent manifested = resolveCloudform(new Forest());

        assertThatThrownBy(() -> harness.turnFaceUp(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not a creature card");
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void stillManifestsWhenCloudformIsDestroyedBeforeItsTriggerResolves() {
        harness.setLibrary(player1, List.of(new WhispererOfTheWilds()));
        harness.castFromHand(player1, new Cloudform(), "{1}{U}{U}");
        harness.passBothPriorities();
        var cloudformId = harness.getPermanentId(player1, "Cloudform");

        harness.setHand(player2, List.of(new ReturnToTheEarth()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, cloudformId);
        harness.assertInGraveyard(player1, "Cloudform");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isManifested()).isTrue();
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void emptyLibraryLeavesCloudformUnattachedAndPutsItIntoGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Cloudform(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Cloudform");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cloudform");
        harness.assertInGraveyard(player1, "Cloudform");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void destroyingCloudformRemovesGrantedKeywordsButLeavesManifestedCreature() {
        Permanent manifested = resolveCloudform(new WhispererOfTheWilds());
        var cloudformId = harness.getPermanentId(player1, "Cloudform");
        harness.setHand(player2, List.of(new ReturnToTheEarth()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, cloudformId);

        harness.assertInGraveyard(player1, "Cloudform");
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(manifested);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, manifested, Keyword.HEXPROOF)).isFalse();
    }

    private Permanent resolveCloudform(Card topCard) {
        harness.setLibrary(player1, List.of(topCard));
        harness.castFromHand(player1, new Cloudform(), "{1}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown)
                .findFirst()
                .orElseThrow();
    }
}
