package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NessianHornbeetle;
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

@CardUsed({SentinelsEyes.class, NessianHornbeetle.class})
class SentinelsEyesTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and vigilance")
    void enchantedCreatureGetsBoostAndVigilance() {
        Permanent bears = addReadyBear();
        Permanent aura = new Permanent(new SentinelsEyes());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Can cast Sentinel's Eyes from hand without exiling graveyard cards")
    void castsFromHandWithoutGraveyardExile() {
        Permanent bears = addReadyBear();
        harness.setHand(player1, List.of(new SentinelsEyes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof SentinelsEyes
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Escape exiles two other cards and attaches Sentinel's Eyes")
    void escapeExilesTwoOtherCards() {
        Permanent bears = addReadyBear();
        SentinelsEyes aura = new SentinelsEyes();
        NessianHornbeetle first = new NessianHornbeetle();
        NessianHornbeetle second = new NessianHornbeetle();
        harness.setGraveyard(player1, List.of(aura, first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, bears.getId(), List.of(), List.of(1, 2), null);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == aura
                        && bears.getId().equals(permanent.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Escape requires two other cards in the graveyard")
    void escapeRequiresTwoOtherCards() {
        Permanent bears = addReadyBear();
        harness.setGraveyard(player1, List.of(new SentinelsEyes(), new NessianHornbeetle()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, bears.getId(), List.of(), List.of(0), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new SentinelsEyes());
        harness.setHand(player1, List.of(new SentinelsEyes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent aura = findPermanent(player1, "Sentinel's Eyes");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, aura.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadyBear() {
        return addCreatureReady(player1, new NessianHornbeetle());
    }

    @Test
    @DisplayName("Escaping marks the resulting Aura as escaped")
    void escapedAuraRetainsEscapeStatus() {
        Permanent bears = addReadyBear();
        harness.setGraveyard(player1, List.of(
                new SentinelsEyes(), new NessianHornbeetle(), new NessianHornbeetle()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        gs.playFlashbackSpell(gd, player1, 0, null, bears.getId(), List.of(), List.of(1, 2), null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sentinel's Eyes").isEscaped()).isTrue();
    }

    @Test
    @DisplayName("Can enchant an opponent's creature without boosting other creatures")
    void enchantsOpponentsCreature() {
        Permanent ownBear = addReadyBear();
        Permanent opposingBear = addCreatureReady(player2, new NessianHornbeetle());
        harness.setHand(player1, List.of(new SentinelsEyes()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, opposingBear.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Sentinel's Eyes").getAttachedTo()).isEqualTo(opposingBear.getId());
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opposingBear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Escape cannot exile Sentinel's Eyes itself")
    void escapeCannotExileItself() {
        Permanent bears = addReadyBear();
        SentinelsEyes aura = new SentinelsEyes();
        NessianHornbeetle first = new NessianHornbeetle();
        NessianHornbeetle second = new NessianHornbeetle();
        harness.setGraveyard(player1, List.of(aura, first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, bears.getId(), List.of(), List.of(0, 1), null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(aura, first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Escape must exile two distinct other cards")
    void escapeRejectsDuplicateExileCards() {
        Permanent bears = addReadyBear();
        SentinelsEyes aura = new SentinelsEyes();
        NessianHornbeetle first = new NessianHornbeetle();
        NessianHornbeetle second = new NessianHornbeetle();
        harness.setGraveyard(player1, List.of(aura, first, second));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> gs.playFlashbackSpell(
                gd, player1, 0, null, bears.getId(), List.of(), List.of(1, 1), null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(aura, first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
