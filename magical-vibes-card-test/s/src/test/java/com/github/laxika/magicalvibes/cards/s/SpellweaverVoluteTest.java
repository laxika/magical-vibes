package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.Ghostfire;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.m.MysticSpeculation;
import com.github.laxika.magicalvibes.cards.p.PatriciansScorn;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellweaverVolute.class, Ghostfire.class, MysticSpeculation.class,
        PatriciansScorn.class, TormodsCrypt.class, GrafdiggersCage.class})
class SpellweaverVoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to an instant card in a graveyard")
    void entersAttachedToInstantCardInGraveyard() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        assertThat(volute.getAttachedTo()).isEqualTo(enchantedCard.getId());
    }

    @Test
    @DisplayName("Copies the enchanted instant and reattaches to another instant when cast")
    void copiesAndReattachesAfterCastingCopy() {
        Ghostfire enchantedCard = new Ghostfire();
        Ghostfire anotherInstant = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard, anotherInstant));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(anotherInstant.getId()));

        assertThat(volute.getAttachedTo()).isEqualTo(anotherInstant.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .contains(enchantedCard.getId());
        assertThat(gd.stack).anyMatch(entry ->
                entry.getCard().getName().equals("Ghostfire") && entry.isCopy());
    }

    @Test
    @DisplayName("Declining to cast the copy leaves the enchanted instant attached")
    void decliningToCastCopyLeavesEnchantedInstantAttached() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(volute.getAttachedTo()).isEqualTo(enchantedCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchantedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .doesNotContain(enchantedCard.getId());
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
    }

    @Test
    @DisplayName("Does not trigger when an opponent casts a sorcery")
    void opponentSorceryDoesNotTrigger() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MysticSpeculation(), "{U}");

        assertThat(gd.stack)
                .noneMatch(entry -> entry.getCard().getName().equals("Spellweaver Volute"));
    }

    @Test
    @DisplayName("Does not trigger when the controller casts an instant")
    void controllerInstantDoesNotTrigger() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Ghostfire()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack)
                .noneMatch(entry -> entry.getCard().getName().equals("Spellweaver Volute"));
    }

    @Test
    @DisplayName("Copies the last enchanted card after Volute leaves before that card is exiled")
    void copiesLastEnchantedCardAfterAuraThenCardLeave() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.castFromHand(player1, new PatriciansScorn(), "{3}{W}");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spellweaver Volute");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.passBothPriorities();
        acceptCopyCastChoices();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Ghostfire"));
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Grafdigger's Cage prevents casting the copy from the graveyard")
    void graveyardCastingRestrictionPreventsCopyCast() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();
        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        harness.addToBattlefield(player2, new GrafdiggersCage());

        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.passBothPriorities();
        acceptCopyCastChoices();

        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(volute.getAttachedTo()).isEqualTo(enchantedCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(enchantedCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(enchantedCard);
    }

    @Test
    @DisplayName("Volute goes to the graveyard when the cast copy leaves no instant to enchant")
    void noRemainingInstantPutsAuraInGraveyard() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.passBothPriorities();
        acceptCopyCastChoices();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(enchantedCard);
        harness.assertNotOnBattlefield(player1, "Spellweaver Volute");
        harness.assertInGraveyard(player1, "Spellweaver Volute");
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Exiling the enchanted card before the trigger resolves creates no copy")
    void enchantedCardLeavingBeforeAuraCreatesNoCopy() {
        Ghostfire enchantedCard = new Ghostfire();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new TormodsCrypt());

        harness.castFromHand(player1, new MysticSpeculation(), "{U}");
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spellweaver Volute");
        harness.passBothPriorities();
        acceptCopyCastChoices();

        assertThat(gd.stack).noneMatch(entry -> entry.isCopy());
        assertThat(gd.interaction.activeInteraction()).isNotInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.assertLife(player2, 20);
    }

    private void acceptCopyCastChoices() {
        for (int choice = 0; choice < 2
                && gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice; choice++) {
            harness.handleMayAbilityChosen(player1, true);
        }
    }
}
