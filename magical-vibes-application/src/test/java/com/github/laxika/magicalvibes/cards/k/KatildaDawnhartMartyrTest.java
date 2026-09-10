package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KatildaDawnhartMartyr.class, KatildasRisingDawn.class, GrizzlyBears.class})
class KatildaDawnhartMartyrTest extends BaseCardTest {

    @Test
    @DisplayName("Katilda's power and toughness count Spirits and enchantments without double-counting")
    void countsSpiritsAndEnchantments() {
        Permanent katilda = harness.addToBattlefieldAndReturn(player1, new KatildaDawnhartMartyr());

        assertThat(gqs.getEffectivePower(gd, katilda)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, katilda)).isEqualTo(1);

        harness.addToBattlefield(player1, card("Spirit", CardType.CREATURE, CardSubtype.SPIRIT));
        harness.addToBattlefield(player1, card("Enchantment", CardType.ENCHANTMENT));
        harness.addToBattlefield(player1, spiritEnchantment());

        assertThat(gqs.getEffectivePower(gd, katilda)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, katilda)).isEqualTo(4);
    }

    @Test
    @DisplayName("Disturb casts Katilda transformed as an Aura with dynamic granted abilities")
    void disturbCreatesDynamicAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, card("Spirit", CardType.CREATURE, CardSubtype.SPIRIT));
        Permanent aura = castDisturb(bears);

        assertThat(aura.isTransformed()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();

        Card vampire = card("Vampire", CardType.CREATURE, CardSubtype.VAMPIRE);
        Card human = card("Human", CardType.CREATURE, CardSubtype.HUMAN);
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, bears, new Permanent(vampire))).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, bears, new Permanent(human))).isFalse();
    }

    @Test
    @DisplayName("The transformed Aura is exiled instead of going to the graveyard")
    void transformedAuraIsExiledInsteadOfGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = castDisturb(bears);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(exiled -> exiled.card().getId()))
                .contains(aura.getOriginalCard().getId());
    }

    @Test
    @DisplayName("Disturb requires a creature target")
    void disturbRequiresCreatureTarget() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new KatildaDawnhartMartyr()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");
    }

    private Permanent castDisturb(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new KatildaDawnhartMartyr()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0, target.getId());
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isTransformed)
                .findFirst()
                .orElseThrow();
    }

    private static Card card(String name, CardType type, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setPower(type == CardType.CREATURE ? 1 : null);
        card.setToughness(type == CardType.CREATURE ? 1 : null);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card spiritEnchantment() {
        Card card = card("Spirit Enchantment", CardType.CREATURE, CardSubtype.SPIRIT);
        card.setAdditionalTypes(Set.of(CardType.ENCHANTMENT));
        return card;
    }
}
