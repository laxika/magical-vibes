package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Brainstorm;
import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.d.DutifulKnowledgeSeeker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LostInTheSpiritWorld;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanShiTongAllKnowing.class, GrizzlyBears.class, InnocenceKami.class, Island.class,
        Brainstorm.class, Consider.class, DutifulKnowledgeSeeker.class, LostInTheSpiritWorld.class})
class WanShiTongAllKnowingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts the target second from the top and creates two Spirit tokens")
    void etbTucksTargetAndCreatesSpirits() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        castWanShiTong(target);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Second from the top");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Grizzly Bears", "Island");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("The ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new WanShiTongAllKnowing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spirit tokens can block and be blocked only by Spirit creatures")
    void spiritTokenCombatRestriction() {
        castWanShiTongAndResolve();
        Permanent token = findPermanents(player1, "Spirit").getFirst();
        Permanent nonSpirit = addCreatureReady(player2, new GrizzlyBears());
        Permanent spirit = addCreatureReady(player2, new InnocenceKami());

        assertThat(bls.canBlockAttacker(gd, nonSpirit, token,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, spirit, token,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, token, nonSpirit,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, token, spirit,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    private void castWanShiTong(Permanent target) {
        harness.setHand(player1, List.of(new WanShiTongAllKnowing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
    }

    private void castWanShiTongAndResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Island()));
        castWanShiTong(target);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Second from the top");
        harness.passBothPriorities();
    }

    @Test
    void ownerCanChooseBottomAndCreatesOnlyTwoSpirits() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DutifulKnowledgeSeeker());
        Card first = new Brainstorm();
        Card second = new Consider();
        harness.setLibrary(player2, List.of(first, second));

        castWanShiTong(target);
        resolveAllTriggers();
        assertThatThrownBy(() -> harness.handleListChoice(player1, "Bottom"))
                .isInstanceOf(IllegalStateException.class);
        harness.handleListChoice(player2, "Bottom");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second, target.getCard());
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Dutiful Knowledge Seeker");
    }

    @Test
    void secondFromTopOfEmptyLibraryBecomesOnlyCard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DutifulKnowledgeSeeker());
        harness.setLibrary(player2, List.of());

        castWanShiTong(target);
        resolveAllTriggers();
        harness.handleListChoice(player2, "Second from the top");
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard());
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void puttingTokenIntoLibraryDoesNotCreateSpirits() {
        harness.setHand(player1, List.of(new LostInTheSpiritWorld()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, List.of());
        Permanent token = findPermanents(player1, "Spirit").getFirst();
        Card libraryCard = new Brainstorm();
        harness.setLibrary(player1, List.of(libraryCard));

        castWanShiTong(token);
        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.TargetLibraryDestinationChoice) {
            harness.handleListChoice(player1, "Second from the top");
            resolveAllTriggers();
        }

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.assertOnBattlefield(player1, "Wan Shi Tong, All-Knowing");
    }

    @Test
    void twoCardsReturnedFromHandTriggerOnce() {
        harness.addToBattlefield(player1, new WanShiTongAllKnowing());
        Card first = new Consider();
        Card second = new DutifulKnowledgeSeeker();
        Card third = new LostInTheSpiritWorld();
        Card fourth = new Brainstorm();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        harness.castFromHand(player1, new Brainstorm(), "{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, fourth);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void opponentReturningCardsFromHandCreatesSpiritsForWanShiTongsController() {
        harness.addToBattlefield(player1, new WanShiTongAllKnowing());
        Card first = new Consider();
        Card second = new DutifulKnowledgeSeeker();
        Card third = new LostInTheSpiritWorld();
        harness.setLibrary(player2, List.of(first, second, third));

        harness.castFromHand(player2, new Brainstorm(), "{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player2, List.of(first.getId(), second.getId()));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(first, second);
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void cardReturnedFromOpponentsGraveyardCreatesTwoSpirits() {
        harness.addToBattlefield(player1, new WanShiTongAllKnowing());
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new DutifulKnowledgeSeeker());
        Card target = new Brainstorm();
        Card top = new Consider();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(top));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(seeker),
                null, target.getId(), Zone.GRAVEYARD);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, target);
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    void surveilingAndDrawingDoNotCreateSpirits() {
        harness.addToBattlefield(player1, new WanShiTongAllKnowing());
        Card top = new Brainstorm();
        Card second = new DutifulKnowledgeSeeker();
        harness.setLibrary(player1, List.of(top, second));

        harness.castFromHand(player1, new Consider(), "{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }
}
