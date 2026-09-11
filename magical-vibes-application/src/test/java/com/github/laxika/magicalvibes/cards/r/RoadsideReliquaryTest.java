package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoadsideReliquary.class, GrizzlyBears.class, Spellbook.class, AuraFlux.class})
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
}
