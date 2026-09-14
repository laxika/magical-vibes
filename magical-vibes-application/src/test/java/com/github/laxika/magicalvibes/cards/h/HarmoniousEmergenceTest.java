package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.Demolish;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarmoniousEmergence.class, Demolish.class, Forest.class})
class HarmoniousEmergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted land becomes a 4/5 green Spirit creature with vigilance and haste")
    void animatesEnchantedLand() {
        Permanent forest = addEnchantedForest();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.computeStaticBonus(gd, forest).grantedSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("When enchanted land would be destroyed, the Aura is sacrificed and the land gains indestructible")
    void sacrificesAuraAndGrantsIndestructible() {
        Permanent forest = addEnchantedForest();

        harness.setHand(player2, List.of(new Demolish()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Harmonious Emergence");
        assertThat(gqs.hasKeyword(gd, forest, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("Harmonious Emergence can enchant only a land controlled by its caster")
    void targetMustBeLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());
        Permanent opponentForest = findPermanent(player2, "Forest");
        harness.setHand(player1, List.of(new HarmoniousEmergence()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land you control");
    }

    private Permanent addEnchantedForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = findPermanent(player1, "Forest");
        Permanent aura = new Permanent(new HarmoniousEmergence());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return forest;
    }
}
