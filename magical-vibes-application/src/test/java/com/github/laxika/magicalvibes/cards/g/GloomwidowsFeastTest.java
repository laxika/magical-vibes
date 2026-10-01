package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BriarberryCohort;
import com.github.laxika.magicalvibes.cards.f.FaerieMacabre;
import com.github.laxika.magicalvibes.cards.r.RuneCervinRider;
import com.github.laxika.magicalvibes.cards.s.SafeholdElite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GloomwidowsFeast.class, BriarberryCohort.class, FaerieMacabre.class,
        RuneCervinRider.class, SafeholdElite.class})
class GloomwidowsFeastTest extends BaseCardTest {

    // ===== Blue flyer: destroyed + Spider token created =====

    @Test
    @DisplayName("Blue flying creature is destroyed and a 1/2 green Spider with reach is created")
    void blueFlyerDestroyedAndTokenCreated() {
        UUID target = addCreature(player2, new BriarberryCohort()); // blue, flying
        castFeast(target);

        harness.assertNotOnBattlefield(player2, "Briarberry Cohort");
        harness.assertInGraveyard(player2, "Briarberry Cohort");

        Permanent spider = findPermanent(player1, "Spider");
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
    }

    // ===== Black flyer: destroyed + Spider token created =====

    @Test
    @DisplayName("Black flying creature is destroyed and a Spider token is created")
    void blackFlyerDestroyedAndTokenCreated() {
        UUID target = addCreature(player2, new FaerieMacabre()); // black, flying
        castFeast(target);

        harness.assertNotOnBattlefield(player2, "Faerie Macabre");
        harness.assertInGraveyard(player2, "Faerie Macabre");
        assertThat(findPermanents(player1, "Spider")).hasSize(1);
    }

    // ===== Non-blue-non-black flyer: destroyed, no token =====

    @Test
    @DisplayName("White flying creature is destroyed but no Spider token is created")
    void whiteFlyerDestroyedNoToken() {
        UUID target = addCreature(player2, new RuneCervinRider()); // white, flying
        castFeast(target);

        harness.assertNotOnBattlefield(player2, "Rune-Cervin Rider");
        harness.assertInGraveyard(player2, "Rune-Cervin Rider");
        assertThat(findPermanents(player1, "Spider")).isEmpty();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        UUID nonFlyer = harness.addToBattlefieldAndReturn(player2, new SafeholdElite()).getId();
        harness.setHand(player1, List.of(new GloomwidowsFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonFlyer))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private UUID addCreature(Player owner, Card card) {
        return harness.addToBattlefieldAndReturn(owner, card).getId();
    }

    private void castFeast(UUID targetId) {
        harness.setHand(player1, List.of(new GloomwidowsFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
