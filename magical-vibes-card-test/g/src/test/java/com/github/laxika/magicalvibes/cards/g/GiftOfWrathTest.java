package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EcologistsTerrarium;
import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiftOfWrath.class, EcologistsTerrarium.class, JukaiTrainee.class, Mountain.class})
class GiftOfWrathTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an enchanted creature and grants menace")
    void boostsEnchantedCreatureAndGrantsMenace() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());

        castGiftOfWrath(bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Does not boost an enchanted noncreature artifact")
    void doesNotBoostNoncreatureArtifact() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player1, new EcologistsTerrarium());

        castGiftOfWrath(fountain.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, fountain)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, fountain)).isZero();
        assertThat(gqs.hasKeyword(gd, fountain, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Creates a red 2/2 Spirit token with menace when it leaves")
    void createsSpiritTokenWhenLeaving() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GiftOfWrath());
        aura.setAttachedTo(bears.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        List<Permanent> spirits = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SPIRIT))
                .toList();
        assertThat(spirits).hasSize(1);
        assertThat(spirits.getFirst().getCard().getPower()).isEqualTo(2);
        assertThat(spirits.getFirst().getCard().getToughness()).isEqualTo(2);
        assertThat(spirits.getFirst().getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED);
        assertThat(spirits.getFirst().getCard().getKeywords()).contains(Keyword.MENACE);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature nonartifact permanent")
    void rejectsInvalidTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new GiftOfWrath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact or creature");
    }

    private void castGiftOfWrath(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new GiftOfWrath()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, targetId);
    }

    @Test
    @DisplayName("Creates its controller's token when an opposing enchanted creature dies")
    void createsTokenForAuraControllerWhenEnchantedCreatureDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new JukaiTrainee());
        castGiftOfWrath(creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gift of Wrath");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Returning the Aura to hand creates a token and removes its bonuses")
    void createsTokenWhenReturnedToHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        castGiftOfWrath(creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Gift of Wrath");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gift of Wrath");
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("An Aura spell whose target disappears creates no token")
    void doesNotCreateTokenWhenSpellTargetDisappears() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new JukaiTrainee());
        castGiftOfWrath(creature.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gift of Wrath");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Exiling the Aura from a noncreature artifact still creates a Spirit")
    void createsTokenWhenExiledFromNoncreatureArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EcologistsTerrarium());
        castGiftOfWrath(artifact.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Gift of Wrath");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, aura));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gift of Wrath"));
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        harness.assertOnBattlefield(player1, "Ecologist's Terrarium");
    }
}
