package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HondenOfLifesWeb;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DiversionSpecialist.class, GrizzlyBears.class, HondenOfLifesWeb.class, Spellbook.class})
class DiversionSpecialistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature exiles the top card and lets it be played this turn")
    void sacrificesCreatureAndExilesTopCard() {
        Permanent specialist = addReady(new DiversionSpecialist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
        assertThat(gd.exilePlayPermissions).containsEntry(top.getId(), player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(top.getId());
    }

    @Test
    @DisplayName("The activation can sacrifice an enchantment")
    void sacrificesEnchantment() {
        Permanent specialist = addReady(new DiversionSpecialist());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HondenOfLifesWeb());
        Card top = putCardOnTop(player1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(top);
    }

    @Test
    @DisplayName("The sacrifice choice only allows another creature or enchantment")
    void sacrificeChoiceFiltersPermanents() {
        Permanent specialist = addReady(new DiversionSpecialist());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new HondenOfLifesWeb());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
    }

    @Test
    @DisplayName("The activation cannot sacrifice an artifact")
    void cannotSacrificeArtifact() {
        Permanent specialist = addReady(new DiversionSpecialist());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(specialist), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    private Permanent addReady(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

    private Card putCardOnTop(Player player) {
        Card card = new Card();
        card.setName("Exiled Card");
        gd.playerDecks.get(player.getId()).addFirst(card);
        return card;
    }
}
