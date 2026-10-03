package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DaxosBlessedByTheSun;
import com.github.laxika.magicalvibes.cards.n.NyxbornColossus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HyraxTowerScout;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.cards.s.SetessanTraining;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CalixDestinysHand.class, DaxosBlessedByTheSun.class, NyxbornColossus.class,
        Forest.class, HyraxTowerScout.class,
        ReturnToNature.class, SetessanTraining.class, UnderworldDreams.class})
class CalixDestinysHandTest extends BaseCardTest {

    @Test
    @DisplayName("+1 puts a revealed enchantment into hand and randomizes the rest on the bottom")
    void plusOneFindsEnchantment() {
        Permanent calix = addReadyCalix(4);
        Card enchantment = new UnderworldDreams();
        Card creature = new HyraxTowerScout();
        Card land = new Forest();
        Card instant = new ReturnToNature();
        harness.setLibrary(player1, List.of(enchantment, creature, land, instant));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(enchantment.getId()));

        assertThat(calix.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).contains(enchantment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land, instant);
    }

    @Test
    @DisplayName("-3 exiles the first target until the second target enchantment leaves")
    void minusThreeUsesTheEnchantmentAsDurationAnchor() {
        Permanent calix = addReadyCalix(4);
        Permanent creature = addPermanent(player2, new HyraxTowerScout());
        Permanent enchantment = addPermanent(player1, new UnderworldDreams());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(creature.getId(), enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNotNull();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, calix));
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, enchantment));

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(
                permanent -> permanent.getCard() == creature.getOriginalCard());
    }

    @Test
    @DisplayName("-3 requires an opposing creature or enchantment and a controlled enchantment")
    void minusThreeTargetRestrictions() {
        addReadyCalix(4);
        Permanent ownCreature = addPermanent(player1, new HyraxTowerScout());
        Permanent opposingLand = addPermanent(player2, new Forest());
        Permanent enchantment = addPermanent(player1, new UnderworldDreams());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(ownCreature.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(opposingLand.getId(), enchantment.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 returns all enchantment cards from the controller's graveyard")
    void minusSevenReturnsAllEnchantments() {
        addReadyCalix(7);
        Card firstEnchantment = new UnderworldDreams();
        Card secondEnchantment = new UnderworldDreams();
        Card creature = new HyraxTowerScout();
        harness.setGraveyard(player1, List.of(firstEnchantment, creature, secondEnchantment));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard())
                .contains(firstEnchantment, secondEnchantment);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(creature)
                .doesNotContain(firstEnchantment, secondEnchantment);
    }

    @Test
    void plusOneMayDeclineWithShortLibrary() {
        addReadyCalix(4);
        Card enchantment = new UnderworldDreams();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(enchantment, land));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(enchantment, land);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(enchantment, land);
    }

    @Test
    void minusThreeDoesNotExileWhenAnchorLeavesBeforeResolution() {
        addReadyCalix(4);
        Permanent creature = addPermanent(player2, new HyraxTowerScout());
        Permanent anchor = addPermanent(player1, new UnderworldDreams());
        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(creature.getId(), anchor.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, anchor));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.findExiledCard(creature.getOriginalCard().getId())).isNull();
    }

    @Test
    void minusThreeCanExileOpposingNoncreatureEnchantment() {
        addReadyCalix(4);
        Permanent target = addPermanent(player2, new UnderworldDreams());
        Permanent anchor = addPermanent(player1, new UnderworldDreams());
        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(target.getId(), anchor.getId()));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, anchor));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard() == target.getOriginalCard());
    }

    @Test
    void minusSevenReturnsAuraAttachedToChosenCreature() {
        addReadyCalix(7);
        Permanent first = addPermanent(player1, new HyraxTowerScout());
        Permanent chosen = addPermanent(player1, new HyraxTowerScout());
        Card aura = new SetessanTraining();
        harness.setGraveyard(player1, List.of(aura));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), chosen.getId());
        harness.handlePermanentChosen(player1, chosen.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(aura);
                    assertThat(permanent.getAttachedTo()).isEqualTo(chosen.getId());
                });
    }

    @Test
    void minusSevenLeavesAuraInGraveyardWithoutLegalAttachment() {
        addReadyCalix(7);
        Card aura = new SetessanTraining();
        harness.setGraveyard(player1, List.of(aura));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(aura);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == aura);
    }

    @Test
    void minusSevenReturnsEnchantmentCreaturesSimultaneously() {
        addReadyCalix(7);
        harness.setLife(player1, 20);
        Card creature = new NyxbornColossus();
        Card daxos = new DaxosBlessedByTheSun();
        harness.setGraveyard(player1, List.of(creature, daxos));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).contains(creature, daxos);
        harness.assertLife(player1, 21);
    }

    private Permanent addReadyCalix(int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new CalixDestinysHand());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
