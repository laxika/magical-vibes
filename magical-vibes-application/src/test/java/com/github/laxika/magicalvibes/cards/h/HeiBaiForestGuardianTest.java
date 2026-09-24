package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeiBaiForestGuardian.class, GrizzlyBears.class})
class HeiBaiForestGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals to a Shrine and may put it onto the battlefield")
    void entersShrineOntoBattlefield() {
        Card shrine = shrine("Test Shrine");
        harness.setHand(player1, List.of(new HeiBaiForestGuardian()));
        harness.setLibrary(player1, List.of(nonShrine(), shrine));
        addCreatureManaForHeiBai();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Test Shrine");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Declining the Shrine placement shuffles the revealed cards back")
    void declinesShrineOntoBattlefield() {
        Card shrine = shrine("Test Shrine");
        harness.setHand(player1, List.of(new HeiBaiForestGuardian()));
        harness.setLibrary(player1, List.of(nonShrine(), shrine));
        addCreatureManaForHeiBai();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Test Shrine");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(shrine);
    }

    @Test
    @DisplayName("Creates one Spirit for each legendary enchantment")
    void createsSpiritForEachLegendaryEnchantment() {
        Permanent guardian = addCreatureReady(player1, new HeiBaiForestGuardian());
        harness.addToBattlefield(player1, legendaryEnchantment("First Legendary Enchantment"));
        harness.addToBattlefield(player1, legendaryEnchantment("Second Legendary Enchantment"));
        addActivationMana(player1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guardian), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("Spirit tokens can only be blocked by Spirits")
    void spiritTokensCanOnlyBeBlockedBySpirits() {
        Permanent spirit = createSpirit(player1);
        spirit.setSummoningSick(false);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        spirit.setAttacking(true);

        prepareDeclareBlockers(player1);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(spirit)))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spirit tokens can only block Spirits")
    void spiritTokensCanOnlyBlockSpirits() {
        Permanent defendingSpirit = createSpirit(player1);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        defendingSpirit.setSummoningSick(false);
        prepareDeclareBlockers(player2);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(defendingSpirit),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCreatureManaForHeiBai() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private Permanent createSpirit(Player player) {
        Permanent guardian = addCreatureReady(player, new HeiBaiForestGuardian());
        harness.addToBattlefield(player, legendaryEnchantment("Legendary Enchantment"));
        addActivationMana(player);
        harness.activateAbility(player, gd.playerBattlefields.get(player.getId()).indexOf(guardian), null, null);
        harness.passBothPriorities();
        return findPermanents(player, "Spirit").getFirst();
    }

    private void addActivationMana(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }

    private Card shrine(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSubtypes(List.of(CardSubtype.SHRINE));
        return card;
    }

    private Card nonShrine() {
        Card card = new Card();
        card.setName("Non-Shrine Card");
        card.setType(CardType.CREATURE);
        return card;
    }

    private Card legendaryEnchantment(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return card;
    }
}
