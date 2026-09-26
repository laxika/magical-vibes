package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.s.SenseisDiviningTop;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OniPossession.class, HumbleBudoka.class, WanderingOnes.class, SenseisDiviningTop.class})
class OniPossessionTest extends BaseCardTest {

    private Permanent attachPossession(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new OniPossession());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +3/+3 and has trample")
    void grantsBoostAndTrample() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka()); // 2/2
        attachPossession(player1, budoka);

        assertThat(gqs.getEffectivePower(gd, budoka)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, budoka)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, budoka, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is a Demon Spirit, replacing its printed creature types")
    void replacesCreatureTypes() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka()); // Human Monk
        attachPossession(player1, budoka);

        assertThat(gqs.effectiveCreatureSubtypes(gd, budoka))
                .containsExactlyInAnyOrder(CardSubtype.DEMON, CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Bonuses and type change fall off when the Aura leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        Permanent aura = attachPossession(player1, budoka);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, budoka)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, budoka, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, budoka))
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.MONK);
    }

    @Test
    @DisplayName("At the beginning of your upkeep, the only creature is auto-sacrificed")
    void upkeepAutoSacrificesOnlyCreature() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        attachPossession(player1, budoka);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(budoka.getId()));
        harness.assertInGraveyard(player1, "Humble Budoka");
    }

    @Test
    @DisplayName("With several creatures the controller chooses, including the enchanted one")
    void upkeepControllerChooses() {
        Permanent enchantedBudoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        Permanent otherBudoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        attachPossession(player1, enchantedBudoka);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(enchantedBudoka.getId(), otherBudoka.getId());

        harness.handlePermanentChosen(player1, enchantedBudoka.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(enchantedBudoka.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(otherBudoka.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        attachPossession(player1, budoka);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(budoka.getId()));
    }

    @Test
    @DisplayName("Sacrificing the enchanted creature puts the Aura into the graveyard")
    void sacrificingEnchantedCreatureKillsAura() {
        Permanent budoka = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        attachPossession(player1, budoka);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof OniPossession);
        harness.assertInGraveyard(player1, "Oni Possession");
    }

    @Test
    @DisplayName("Only the Aura's controller sacrifices, even when enchanting an opponent's creature")
    void onlyControllerSacrifices() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        attachPossession(player1, opponentCreature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownCreature.getId()));
    }

    @Test
    @DisplayName("Does nothing when the Aura's controller controls no creatures")
    void upkeepDoesNothingWithoutCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new HumbleBudoka());
        Permanent aura = attachPossession(player1, opponentCreature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(aura.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(opponentCreature.getId()));
    }

    @Test
    @DisplayName("Can enchant a creature")
    void canEnchantCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WanderingOnes());
        harness.setHand(player1, List.of(new OniPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Oni Possession");
        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SenseisDiviningTop());
        harness.setHand(player1, List.of(new OniPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot enchant a creature with shroud")
    void cannotEnchantCreatureWithShroud() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HumbleBudoka());
        harness.setHand(player1, List.of(new OniPossession()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
