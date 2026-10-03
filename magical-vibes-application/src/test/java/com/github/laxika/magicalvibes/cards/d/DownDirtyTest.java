package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.m.MazeBehemoth;
import com.github.laxika.magicalvibes.cards.p.PossibilityStorm;
import com.github.laxika.magicalvibes.cards.s.SelesnyaGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DownDirty.class, KraulWarrior.class, MazeBehemoth.class, SelesnyaGuildgate.class,
        PossibilityStorm.class})
class DownDirtyTest extends BaseCardTest {

    private static final int DOWN = 0;
    private static final int DIRTY = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Down makes the targeted player discard two cards")
    void downDiscardsTwoCards() {
        harness.setHand(player2, List.of(new SelesnyaGuildgate(), new MazeBehemoth(), new KraulWarrior()));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, DOWN, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Dirty returns any card from your graveyard to your hand")
    void dirtyReturnsTargetCard() {
        SelesnyaGuildgate target = new SelesnyaGuildgate();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, DIRTY, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Fuse resolves Down before Dirty")
    void fuseResolvesBothHalves() {
        harness.setHand(player2, List.of(new SelesnyaGuildgate(), new MazeBehemoth(), new KraulWarrior()));
        SelesnyaGuildgate target = new SelesnyaGuildgate();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, FUSE, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Fusing with yourself as Down's target discards before returning Dirty's target")
    void fuseCanTargetControllerAndReturnsOnlyAfterDiscarding() {
        SelesnyaGuildgate target = new SelesnyaGuildgate();
        KraulWarrior discarded = new KraulWarrior();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty(), discarded));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, FUSE, List.of(player1.getId(), target.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded).doesNotContain(target);
    }

    @Test
    @DisplayName("Down discards the only card when the target has fewer than two cards")
    void downDiscardsOnlyAvailableCard() {
        KraulWarrior discarded = new KraulWarrior();
        harness.setHand(player2, List.of(discarded));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, DOWN, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Fuse still returns Dirty's target when Down's target has an empty hand")
    void fuseReturnsCardWhenThereIsNothingToDiscard() {
        KraulWarrior target = new KraulWarrior();
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, FUSE, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Fuse still resolves Down when Dirty's graveyard target becomes illegal")
    void fuseDiscardsWhenGraveyardTargetLeaves() {
        KraulWarrior target = new KraulWarrior();
        SelesnyaGuildgate discarded = new SelesnyaGuildgate();
        harness.setHand(player2, List.of(discarded));
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, FUSE, List.of(player2.getId(), target.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(target);
        gd.addToExile(player1.getId(), target);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Down cast from exile pays only Down's mana cost")
    void downFromExilePaysOnlyDownCost() {
        DownDirty spell = new DownDirty();
        KraulWarrior discarded = new KraulWarrior();
        harness.setHand(player2, List.of(discarded));
        harness.setExile(player1, List.of(spell));
        gd.exilePlayPermissions.put(spell.getId(), player1.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFromExile(player1, spell.getId(), player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Possibility Storm offers only individual halves when casting from exile")
    void freeCastFromExileCannotOfferFuse() {
        harness.addToBattlefield(player1, new PossibilityStorm());
        harness.setLibrary(player1, List.of(new DownDirty()));
        harness.setGraveyard(player1, List.of(new KraulWarrior()));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, DOWN, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).hasSize(2).noneMatch(label -> label.startsWith("Fuse"));
    }

    @Test
    @DisplayName("Down cannot target a permanent")
    void downCannotTargetPermanent() {
        UUID permanentId = harness.addToBattlefieldAndReturn(player2, new KraulWarrior()).getId();
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, DOWN, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Dirty cannot target an opponent's graveyard")
    void dirtyCannotTargetOpponentsGraveyard() {
        SelesnyaGuildgate target = new SelesnyaGuildgate();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new DownDirty()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, DIRTY, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
