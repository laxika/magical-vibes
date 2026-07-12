package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GloomwidowsFeastTest extends BaseCardTest {

    // ===== Blue flyer: destroyed + Spider token created =====

    @Test
    @DisplayName("Blue flying creature is destroyed and a 1/2 green Spider with reach is created")
    void blueFlyerDestroyedAndTokenCreated() {
        UUID target = addCreature(player2, new AirElemental()); // blue, flying
        castFeast(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Air Elemental"));

        Permanent spider = spiderToken().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, spider, Keyword.REACH)).isTrue();
    }

    // ===== Black flyer: destroyed + Spider token created =====

    @Test
    @DisplayName("Black flying creature is destroyed and a Spider token is created")
    void blackFlyerDestroyedAndTokenCreated() {
        UUID target = addCreature(player2, new BogImp()); // black, flying
        castFeast(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Bog Imp"));
        assertThat(spiderToken()).isPresent();
    }

    // ===== Non-blue-non-black flyer: destroyed, no token =====

    @Test
    @DisplayName("White flying creature is destroyed but no Spider token is created")
    void whiteFlyerDestroyedNoToken() {
        UUID target = addCreature(player2, new SerraAngel()); // white, flying
        castFeast(target);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Serra Angel"));
        assertThat(spiderToken()).isEmpty();
    }

    // ===== Targeting =====

    @Test
    @DisplayName("Cannot target a creature without flying")
    void cannotTargetNonFlyer() {
        harness.addToBattlefield(player2, new GrizzlyBears()); // no flying
        UUID bears = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new GloomwidowsFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private UUID addCreature(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        harness.addToBattlefield(owner, card);
        return harness.getPermanentId(owner, card.getName());
    }

    private void castFeast(UUID targetId) {
        harness.setHand(player1, List.of(new GloomwidowsFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Optional<Permanent> spiderToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && "Spider".equals(p.getCard().getName()))
                .findFirst();
    }
}
