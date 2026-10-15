package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SarumanOfManyColors.class, Divination.class,
        Forest.class, LightningBolt.class, ProdigalPyromancer.class, LeylineOfTheVoid.class})
class SarumanOfManyColorsTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell mills each opponent and offers a qualifying graveyard copy")
    void secondSpellMillsAndCopiesEligibleSpell() {
        SarumanOfManyColors saruman = new SarumanOfManyColors();
        Divination graveyardTarget = new Divination();
        harness.addToBattlefield(player1, saruman);
        harness.setGraveyard(player2, List.of(graveyardTarget));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(targetChoice).isNotNull();
        assertThat(targetChoice.validCardIds()).containsExactly(graveyardTarget.getId());

        harness.handleMultipleCardsChosen(player1, List.of(graveyardTarget.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest", "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardTarget.getId()));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Ward only permits discarding an enchantment, instant, or sorcery")
    void wardFiltersDiscardChoice() {
        SarumanOfManyColors saruman = new SarumanOfManyColors();
        Forest land = new Forest();
        Divination validDiscard = new Divination();
        var sarumanPermanent = harness.addToBattlefieldAndReturn(player1, saruman);
        harness.setHand(player2, List.of(new LightningBolt(), land, validDiscard));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, sarumanPermanent.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        PendingInteraction.DiscardChoice discardChoice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(discardChoice).isNotNull();
        assertThat(discardChoice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player2, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(saruman.getId()));
    }

    @Test
    @DisplayName("A newly milled card can be targeted and declining the copy leaves the original exiled")
    void newlyMilledCardCanBeTargeted() {
        Divination milledTarget = new Divination();
        harness.addToBattlefield(player1, new SarumanOfManyColors());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player2, List.of(milledTarget, new Forest()));
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(milledTarget.getId());
        harness.handleMultipleCardsChosen(player1, List.of(milledTarget.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(milledTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("The second spell mills even with no legal exile target, and the third spell does not mill")
    void millsWithoutLegalTargetOnlyOnSecondSpell() {
        harness.addToBattlefield(player1, new SarumanOfManyColors());
        harness.setGraveyard(player2, List.of(new Divination()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, player2.getId());
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player2, 11);
    }

    @Test
    @DisplayName("Declining ward counters the opposing spell without discarding")
    void decliningWardCountersSpell() {
        var saruman = harness.addToBattlefieldAndReturn(player1, new SarumanOfManyColors());
        Divination discard = new Divination();
        LightningBolt bolt = new LightningBolt();
        harness.setHand(player2, List.of(bolt, discard));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, saruman.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(discard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bolt);
        assertThat(saruman.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Saruman of Many Colors");
    }

    @Test
    @DisplayName("An empty opposing library does not cause the reflexive exile ability to trigger")
    void noCardsMilledMeansNoExile() {
        Divination target = new Divination();
        harness.addToBattlefield(player1, new SarumanOfManyColors());
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Ward counters an opposing activated ability when its controller has no discard")
    void wardCountersOpposingActivatedAbility() {
        var saruman = harness.addToBattlefieldAndReturn(player1, new SarumanOfManyColors());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.setHand(player2, List.of());

        harness.activateAbility(player2, 0, null, saruman.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(saruman.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Saruman of Many Colors");
    }

    @Test
    @DisplayName("Cards milled into exile still trigger the follow-up targeting an existing graveyard card")
    void millReplacementStillTriggersFollowUp() {
        Divination target = new Divination();
        Forest firstMilled = new Forest();
        Forest secondMilled = new Forest();
        harness.addToBattlefield(player1, new SarumanOfManyColors());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(firstMilled, secondMilled));
        harness.setHand(player1, List.of(new Divination(), new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castSorcery(player1, 0);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstMilled, secondMilled);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(firstMilled, secondMilled, target);
        harness.passBothPriorities();
    }
}
