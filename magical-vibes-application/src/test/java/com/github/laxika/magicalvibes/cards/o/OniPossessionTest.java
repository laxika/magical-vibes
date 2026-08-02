package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OniPossessionTest extends BaseCardTest {

    private Permanent attachPossession(Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new OniPossession());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    @Test
    @DisplayName("Enchanted creature gets +3/+3 and has trample")
    void grantsBoostAndTrample() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        attachPossession(player1, bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature is a Demon Spirit, replacing its printed creature types")
    void replacesCreatureTypes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // Bear
        attachPossession(player1, bears);

        assertThat(gqs.effectiveCreatureSubtypes(gd, bears))
                .containsExactlyInAnyOrder(CardSubtype.DEMON, CardSubtype.SPIRIT);
    }

    @Test
    @DisplayName("Bonuses and type change fall off when the Aura leaves the battlefield")
    void bonusesRemovedWhenAuraLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = attachPossession(player1, bears);

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, bears)).contains(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("At the beginning of your upkeep, the only creature is auto-sacrificed")
    void upkeepAutoSacrificesOnlyCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPossession(player1, bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("With several creatures the controller chooses, including the enchanted one")
    void upkeepControllerChooses() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent spider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attachPossession(player1, bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(bears.getId(), spider.getId());

        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(spider.getId()));
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPossession(player1, bears);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Sacrificing the enchanted creature puts the Aura into the graveyard")
    void sacrificingEnchantedCreatureKillsAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attachPossession(player1, bears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof OniPossession);
        harness.assertInGraveyard(player1, "Oni Possession");
    }

    @Test
    @DisplayName("Only the Aura's controller sacrifices, even when enchanting an opponent's creature")
    void onlyControllerSacrifices() {
        Permanent oppBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent ownSpider = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        attachPossession(player1, oppBears);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(oppBears.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownSpider.getId()));
    }
}
