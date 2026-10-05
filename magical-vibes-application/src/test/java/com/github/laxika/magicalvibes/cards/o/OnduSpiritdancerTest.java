package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OnduSpiritdancer.class, GloriousAnthem.class, Disenchant.class,
        EnchantedEvening.class, Pacifism.class})
class OnduSpiritdancerTest extends BaseCardTest {

    @Test
    @DisplayName("May create a token copy when an enchantment enters under its control")
    void createsTokenCopyWhenAccepted() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();

        resolveAnthemAndTrigger();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(2);
        assertThat(findPermanents(player1, "Glorious Anthem")).anyMatch(permanent ->
                permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Does not create a token copy when the may ability is declined")
    void decliningDoesNotCreateTokenCopy() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();

        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(1);
    }

    @Test
    @DisplayName("Stops triggering after a copy is accepted that turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        harness.setHand(player1, List.of(new GloriousAnthem(), new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castEnchantment(player1, 0);
        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(3);
    }

    private void castAnthem() {
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
    }

    @Test
    @DisplayName("Declining a copy allows a later enchantment to trigger again that turn")
    void canCopyLaterEnchantmentAfterDeclining() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();
        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, false);

        castAnthem();
        resolveAnthemAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple enchantments can trigger before a copy is accepted")
    void queuesEachEntryBeforeAcceptanceButCopiesOnlyOnce() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(3);
    }

    @Test
    @DisplayName("Copies an enchantment using its last known information after it leaves")
    void copiesEnchantmentDestroyedInResponse() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();
        harness.passBothPriorities();
        var anthemId = harness.getPermanentId(player1, "Glorious Anthem");
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, anthemId);
        harness.assertInGraveyard(player1, "Glorious Anthem");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(1);
        assertThat(findPermanents(player1, "Glorious Anthem")).allMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An enchantment Ondu Spiritdancer sees its own entry")
    void triggersForItsOwnEntryWhenItIsAnEnchantment() {
        harness.addToBattlefield(player1, new EnchantedEvening());
        harness.castFromHand(player1, new OnduSpiritdancer(), "{4}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, false);
    }

    @Test
    @DisplayName("A copied Aura enters attached to a legal creature chosen by its controller")
    void copiedAuraAllowsAttachmentChoice() {
        var dancer = harness.addToBattlefieldAndReturn(player1, new OnduSpiritdancer());
        harness.setHand(player1, List.of(new Pacifism()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, dancer.getId());
        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNotNull();
        harness.handlePermanentChosen(player1, dancer.getId());
        assertThat(countPermanents(player1, "Pacifism")).isEqualTo(2);
        assertThat(findPermanents(player1, "Pacifism")).allMatch(permanent ->
                dancer.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("An opponent's enchantment does not trigger the ability")
    void ignoresOpponentsEnchantment() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        harness.enterBattlefieldAndReturn(player2, new GloriousAnthem());

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Glorious Anthem")).isZero();
        assertThat(countPermanents(player2, "Glorious Anthem")).isEqualTo(1);
    }

    @Test
    @DisplayName("The copy allowance resets on the next player's turn")
    void canCopyAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();
        resolveAnthemAndTrigger();
        harness.handleMayAbilityChosen(player1, true);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        harness.enterBattlefieldAndReturn(player1, new GloriousAnthem());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(4);
    }

    @Test
    @DisplayName("Each Ondu Spiritdancer has its own copy allowance")
    void twoSpiritdancersEachCreateACopy() {
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        harness.addToBattlefield(player1, new OnduSpiritdancer());
        castAnthem();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Glorious Anthem")).isEqualTo(3);
    }

    private void resolveAnthemAndTrigger() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
