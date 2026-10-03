package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({CartoucheOfSolidarity.class, ThoseWhoServe.class, Plains.class})
class CartoucheOfSolidarityTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches to a creature you control and creates a Warrior token with vigilance")
    void resolvingAttachesAndCreatesToken() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities(); // resolve aura
        harness.passBothPriorities(); // resolve ETB token trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cartouche of Solidarity")
                        && bears.getId().equals(p.getAttachedTo()));

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
        assertThat(gqs.getEffectiveColors(gd, token)).containsExactly(CardColor.WHITE);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature gets +1/+1 and has first strike")
    void enchantedCreatureBoostedAndFirstStrike() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfSolidarity());
        aura.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creature loses boost and first strike when the Cartouche is removed")
    void effectsStopWhenRemoved() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CartoucheOfSolidarity());
        aura.setAttachedTo(bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot enchant a creature you don't control")
    void cannotEnchantOpponentCreature() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new ThoseWhoServe());
        // A creature you control makes the Aura playable, so casting reaches target validation.
        harness.addToBattlefield(player1, new ThoseWhoServe());

        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent you control")
    void cannotEnchantOwnLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.addToBattlefield(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("An illegal target at resolution prevents both attachment and token creation")
    void targetLeavingBeforeResolutionCreatesNoToken() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cartouche of Solidarity")).isZero();
        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CartoucheOfSolidarity);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token trigger resolves even if the Aura leaves the battlefield")
    void tokenTriggerSurvivesAuraLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(gd.stack).hasSize(1);

        Permanent aura = findPermanent(player1, "Cartouche of Solidarity");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        assertThat(countPermanents(player2, "Warrior")).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Losing control of the target before resolution prevents the Aura from entering")
    void targetChangingControllerBeforeResolutionCreatesNoToken() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Cartouche of Solidarity")).isZero();
        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CartoucheOfSolidarity);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The Aura goes to its owner's graveyard when the enchanted creature changes controller")
    void auraFallsOffAfterCreatureChangesController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new CartoucheOfSolidarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        harness.runStateBasedActions();

        assertThat(countPermanents(player1, "Cartouche of Solidarity")).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof CartoucheOfSolidarity);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
    }
}
