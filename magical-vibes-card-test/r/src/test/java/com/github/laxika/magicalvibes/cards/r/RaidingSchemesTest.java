package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaidingSchemes.class, GiantGrowth.class, GrizzlyBears.class, HowlingMine.class})
class RaidingSchemesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants conspire to a noncreature spell")
    void grantsConspireToNoncreatureSpell() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));

        assertThat(conspireA.isTapped()).isTrue();
        assertThat(conspireB.isTapped()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Does not grant conspire to a creature spell")
    void doesNotGrantConspireToCreatureSpell() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(conspireA.getId(), conspireB.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conspireA.isTapped()).isFalse();
        assertThat(conspireB.isTapped()).isFalse();
    }

    @Test
    void colorlessSpellCannotPayConspire() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent a = addCreatureReady(player1, new GrizzlyBears());
        Permanent b = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new HowlingMine()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(a.getId(), b.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(a.isTapped()).isFalse();
        assertThat(b.isTapped()).isFalse();
    }

    @Test
    void twoSchemesAllowTwoSeparateConspirePayments() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent a = addCreatureReady(player1, new GrizzlyBears());
        Permanent b = addCreatureReady(player1, new GrizzlyBears());
        Permanent c = addCreatureReady(player1, new GrizzlyBears());
        Permanent d = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaidingSchemes()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithConspire(player1, 0, null, List.of(a.getId(), b.getId(), c.getId(), d.getId()));
        assertThat(List.of(a, b, c, d)).allMatch(Permanent::isTapped);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Raiding Schemes")).isEqualTo(5);
        assertThat(findPermanents(player1, "Raiding Schemes").stream()
                .filter(p -> p.getCard().isToken())).hasSize(2);
    }

    @Test
    void copiedEnchantmentResolvesAsToken() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent a = addCreatureReady(player1, new GrizzlyBears());
        Permanent b = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new RaidingSchemes()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithConspire(player1, 0, null, List.of(a.getId(), b.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Raiding Schemes")).isEqualTo(3);
        assertThat(findPermanents(player1, "Raiding Schemes").stream()
                .filter(p -> p.getCard().isToken())).hasSize(1);
    }

    @Test
    void opponentsSchemesDoNotGrantConspire() {
        harness.addToBattlefield(player2, new RaidingSchemes());
        Permanent a = addCreatureReady(player1, new GrizzlyBears());
        Permanent b = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, a.getId(),
                List.of(a.getId(), b.getId()))).isInstanceOf(IllegalStateException.class);
        assertThat(a.isTapped()).isFalse();
        assertThat(b.isTapped()).isFalse();
    }

    @Test
    void conspireIsOptional() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        harness.castFromHand(player1, new RaidingSchemes(), "{3}{R}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Raiding Schemes")).isEqualTo(2);
        assertThat(findPermanents(player1, "Raiding Schemes"))
                .noneMatch(p -> p.getCard().isToken());
    }
}
