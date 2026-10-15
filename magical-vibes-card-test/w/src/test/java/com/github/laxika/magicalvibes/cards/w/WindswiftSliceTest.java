package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Malignus;
import com.github.laxika.magicalvibes.cards.p.PaladinEnVec;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindswiftSlice.class, GrizzlyBears.class, HillGiant.class, Malignus.class, PaladinEnVec.class})
class WindswiftSliceTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one Elf Warrior for one excess damage")
    void createsTokensForExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(source, target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        List<Permanent> tokens = findPermanents(player1, "Elf Warrior");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Creates no tokens when no excess damage is dealt")
    void createsNoTokensWithoutExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(source, target);

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Requires a controlled creature and an opponent's creature")
    void enforcesTargetRestrictions() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent ownTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new WindswiftSlice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(source.getId(), ownTarget.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exact lethal damage creates no tokens")
    void exactLethalDamageCreatesNoTokens() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(source, target);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Previously marked damage increases excess damage")
    void previouslyMarkedDamageIncreasesExcessDamage() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        target.setMarkedDamage(2);
        cast(source, target);

        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(2);
    }

    @Test
    @DisplayName("No damage or tokens when the source leaves before resolution")
    void noDamageWhenSourceLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(source);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("No tokens when the victim leaves before resolution")
    void noTokensWhenVictimLeaves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castInstant(player1, 0, List.of(source.getId(), target.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
        harness.assertOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Protection from the creature's color prevents damage and tokens")
    void protectionPreventsDamageAndTokens() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        cast(source, target);

        harness.assertOnBattlefield(player2, "Paladin en-Vec");
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(findPermanents(player1, "Elf Warrior")).isEmpty();
    }

    @Test
    @DisplayName("Unpreventable creature damage bypasses protection")
    void unpreventableDamageBypassesProtection() {
        harness.setLife(player2, 20);
        Permanent source = harness.addToBattlefieldAndReturn(player1, new Malignus());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PaladinEnVec());
        cast(source, target);

        harness.assertInGraveyard(player2, "Paladin en-Vec");
        assertThat(findPermanents(player1, "Elf Warrior")).hasSize(8);
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new WindswiftSlice()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void cast(Permanent source, Permanent target) {
        prepareSpell();
        harness.castAndResolveInstant(player1, 0, List.of(source.getId(), target.getId()));
    }
}
