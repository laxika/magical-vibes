package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.b.BambooGroveArcher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoadsideReliquary.class, GrizzlyBears.class, Spellbook.class, AuraFlux.class,
        BambooGroveArcher.class, NetworkTerminal.class})
class RoadsideReliquaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing draws no cards without an artifact or enchantment")
    void sacrificesWithoutQualifyingPermanent() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("Sacrificing draws one card when controlling an artifact")
    void drawsForArtifact() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());
        harness.addToBattlefield(player1, new Spellbook());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Sacrificing draws one card when controlling an enchantment")
    void drawsForEnchantment() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());
        harness.addToBattlefield(player1, new AuraFlux());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second);
    }

    @Test
    @DisplayName("Sacrificing draws two cards when controlling an artifact and an enchantment")
    void drawsForArtifactAndEnchantment() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new AuraFlux());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Sacrifice and mana are paid before resolution, and multiple qualifying permanents still draw only two")
    void paysCostsBeforeResolutionAndDrawsAtMostTwo() {
        RoadsideReliquary first = new RoadsideReliquary();
        RoadsideReliquary second = new RoadsideReliquary();
        RoadsideReliquary third = new RoadsideReliquary();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RoadsideReliquary());
        harness.addToBattlefield(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new BambooGroveArcher());
        harness.addToBattlefield(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land.getCard());
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("An opponent's artifact and enchantment do not qualify")
    void doesNotCountOpponentsPermanents() {
        RoadsideReliquary first = new RoadsideReliquary();
        harness.addToBattlefield(player1, new RoadsideReliquary());
        harness.addToBattlefield(player2, new NetworkTerminal());
        harness.addToBattlefield(player2, new BambooGroveArcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("An artifact and enchantment gained after activation qualify at resolution")
    void checksNewPermanentsAtResolution() {
        RoadsideReliquary first = new RoadsideReliquary();
        RoadsideReliquary second = new RoadsideReliquary();
        RoadsideReliquary third = new RoadsideReliquary();
        harness.addToBattlefield(player1, new RoadsideReliquary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.addToBattlefield(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new BambooGroveArcher());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    @DisplayName("Losing the artifact before resolution still allows the enchantment draw")
    void losingArtifactDoesNotPreventEnchantmentDraw() {
        RoadsideReliquary first = new RoadsideReliquary();
        RoadsideReliquary second = new RoadsideReliquary();
        harness.addToBattlefield(player1, new RoadsideReliquary());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new NetworkTerminal());
        harness.addToBattlefield(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, artifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }

    @Test
    @DisplayName("Losing the enchantment before resolution still allows the artifact draw")
    void losingEnchantmentDoesNotPreventArtifactDraw() {
        RoadsideReliquary first = new RoadsideReliquary();
        RoadsideReliquary second = new RoadsideReliquary();
        harness.addToBattlefield(player1, new RoadsideReliquary());
        harness.addToBattlefield(player1, new NetworkTerminal());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BambooGroveArcher());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, enchantment));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
    }
}
